package com.amethyst.client.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

/**
 * ПРАВИЛЬНЫЙ трансформер для 1.7 blockhit
 * 
 * В 1.8 Mojang изменил логику - когда игрок использует предмет (блокирует),
 * в метод transformFirstPersonItem передается swingProgress = 0.0f
 * 
 * В 1.7 туда передавался реальный swingProgress даже при блокировании
 * 
 * Мы патчим метод renderItemInFirstPerson чтобы всегда передавать реальный swingProgress
 */
public class BlockhitSwingFixer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.ItemRenderer")) {
            System.out.println("[BlockhitFixer] Patching ItemRenderer for 1.7 blockhit");
            return transformItemRenderer(basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformItemRenderer(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new ItemRendererVisitor(Opcodes.ASM5, classWriter);
        
        classReader.accept(classVisitor, 0);
        return classWriter.toByteArray();
    }
    
    private static class ItemRendererVisitor extends ClassVisitor {
        
        public ItemRendererVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            
            // Патчим renderItemInFirstPerson
            // Deobfuscated: renderItemInFirstPerson
            // Obfuscated: func_78440_a
            if ((name.equals("renderItemInFirstPerson") || name.equals("func_78440_a")) 
                && desc.equals("(F)V")) {
                System.out.println("[BlockhitFixer] Found renderItemInFirstPerson - patching swing parameter");
                return new SwingPatcherVisitor(api, mv);
            }
            
            return mv;
        }
    }
    
    /**
     * Патчит вызовы transformFirstPersonItem чтобы передавать реальный swingProgress
     * вместо 0.0f когда игрок блокирует
     */
    private static class SwingPatcherVisitor extends MethodVisitor {
        
        public SwingPatcherVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            // Ищем вызовы transformFirstPersonItem (или func_178096_b)
            if ((name.equals("transformFirstPersonItem") || name.equals("func_178096_b")) 
                && desc.equals("(FF)V")) {
                
                System.out.println("[BlockhitFixer] Found call to transformFirstPersonItem - patching parameters");
                
                // ВАЖНО: В 1.8 перед вызовом transformFirstPersonItem лежит на стеке:
                // [equipProgress, 0.0f или swingProgress]
                // 
                // Мы хотим заменить 0.0f на реальный swingProgress (var4)
                //
                // Стек перед вызовом: [this, equipProgress, 0.0f/swingProgress]
                // Нам нужно заменить второй параметр на var4 (swingProgress)
                
                // Удаляем второй параметр со стека (0.0f)
                super.visitInsn(Opcodes.POP);
                
                // Загружаем var4 (swingProgress) вместо него
                // var4 = локальная переменная #4 (float)
                super.visitVarInsn(Opcodes.FLOAD, 4);
                
                System.out.println("[BlockhitFixer] Replaced 0.0f with var4 (swingProgress)");
            }
            
            // Вызываем оригинальный метод
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}