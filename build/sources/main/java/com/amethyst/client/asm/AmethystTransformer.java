package com.amethyst.client.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

/**
 * ASM Трансформер для инжекта 1.7 анимаций в ItemRenderer
 * 
 * ПРАВИЛЬНЫЙ ПОДХОД:
 * Инжектим вызов в метод transformFirstPersonItem ПЕРЕД всеми vanilla трансформациями
 * Это позволяет применить наши rotate/translate ДО того как vanilla применит scale
 */
public class AmethystTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.ItemRenderer")) {
            System.out.println("[Animations] Transforming ItemRenderer.transformFirstPersonItem");
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
            
            // КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: Хукаем transformFirstPersonItem вместо renderItemInFirstPerson
            // Deobfuscated: transformFirstPersonItem
            // Obfuscated: func_178096_b
            // Signature: (FF)V - два float параметра (equipProgress, swingProgress)
            if ((name.equals("transformFirstPersonItem") || name.equals("func_178096_b")) 
                && desc.equals("(FF)V")) {
                System.out.println("[Animations] Found transformFirstPersonItem - injecting hook");
                return new TransformMethodVisitor(api, mv);
            }
            
            return mv;
        }
    }
    
    /**
     * Инжектим вызов нашего хука В НАЧАЛО transformFirstPersonItem
     * Это позволяет применить 1.7 трансформации ДО vanilla кода
     */
    private static class TransformMethodVisitor extends MethodVisitor {
        
        public TransformMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Инжектим:
            // AnimationTransformHandler.applyTransforms(equipProgress, swingProgress);
            
            // Параметр 1: equipProgress (float)
            mv.visitVarInsn(Opcodes.FLOAD, 1);
            
            // Параметр 2: swingProgress (float)
            mv.visitVarInsn(Opcodes.FLOAD, 2);
            
            // Вызываем статический метод
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/amethyst/client/AnimationTransformHandler",
                "applyTransforms",
                "(FF)V",
                false
            );
            
            System.out.println("[Animations] Injected call to AnimationTransformHandler.applyTransforms");
        }
    }
}