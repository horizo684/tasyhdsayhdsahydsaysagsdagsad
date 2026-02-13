package com.amethyst.client.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

/**
 * ASM Трансформер для инжекта 1.7 blockhit в ItemRenderer
 * 
 * ПРАВИЛЬНЫЙ ПОДХОД ДЛЯ BLOCKHIT:
 * В 1.8 vanilla ItemRenderer.renderItemInFirstPerson передает swingProgress = 0
 * когда игрок блокируется. Мы перехватываем вызов transformFirstPersonItem
 * и заменяем swingProgress на player.getSwingProgress() чтобы анимация не прерывалась!
 */
public class AmethystTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.ItemRenderer")) {
            System.out.println("[BlockhitTransformer] Transforming ItemRenderer for 1.7 blockhit");
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
            
            // Перехватываем renderItemInFirstPerson
            // Deobfuscated: renderItemInFirstPerson(F)V
            // Obfuscated: func_78440_a(F)V
            if ((name.equals("renderItemInFirstPerson") || name.equals("func_78440_a")) 
                && desc.equals("(F)V")) {
                System.out.println("[BlockhitTransformer] Found renderItemInFirstPerson - injecting blockhit fix");
                return new RenderItemMethodVisitor(api, mv);
            }
            
            return mv;
        }
    }
    
    /**
     * Перехватывает вызовы transformFirstPersonItem и заменяет swingProgress
     * на player.getSwingProgress() чтобы анимация не прерывалась при блокировке
     */
    private static class RenderItemMethodVisitor extends MethodVisitor {
        
        public RenderItemMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            // Ищем вызовы transformFirstPersonItem
            // Deobfuscated: transformFirstPersonItem(FF)V
            // Obfuscated: func_178096_b(FF)V
            if (opcode == Opcodes.INVOKESPECIAL && 
                owner.equals("net/minecraft/client/renderer/ItemRenderer") &&
                (name.equals("transformFirstPersonItem") || name.equals("func_178096_b")) &&
                desc.equals("(FF)V")) {
                
                System.out.println("[BlockhitTransformer] Intercepting transformFirstPersonItem call");
                
                // Сохраняем swingProgress который vanilla передала (возможно 0 при блокировке)
                mv.visitVarInsn(Opcodes.FSTORE, 10); // Сохраняем swingProgress во временную переменную
                mv.visitVarInsn(Opcodes.FSTORE, 11); // Сохраняем equipProgress во временную переменную
                
                // Вызываем наш хук для получения правильного swingProgress
                // AnimationTransformHandler.getCorrectSwingProgress(equipProgress, vanillaSwingProgress)
                mv.visitVarInsn(Opcodes.FLOAD, 11); // equipProgress
                mv.visitVarInsn(Opcodes.FLOAD, 10); // vanilla swingProgress
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/amethyst/client/AnimationTransformHandler",
                    "getCorrectSwingProgress",
                    "(FF)F",
                    false
                );
                
                // Стек теперь: [this, equipProgress, corrected_swingProgress]
                // Но нам нужно: [this, corrected_equipProgress, corrected_swingProgress]
                // Так что загружаем equipProgress снова
                mv.visitVarInsn(Opcodes.FSTORE, 12); // сохраняем corrected swingProgress
                mv.visitVarInsn(Opcodes.FLOAD, 11);  // загружаем equipProgress обратно
                mv.visitVarInsn(Opcodes.FLOAD, 12);  // загружаем corrected swingProgress
                
                // Теперь вызываем оригинальный метод с исправленным swingProgress
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
            
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}