package com.amethyst.client.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class AmethystTransformer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.ItemRenderer")) {
            System.out.println("[AmethystClient] Transforming ItemRenderer");
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
            
            // Transform renderItemInFirstPerson method
            // Deobfuscated: renderItemInFirstPerson, Obfuscated: func_78440_a
            if (name.equals("renderItemInFirstPerson") || name.equals("func_78440_a")) {
                System.out.println("[AmethystClient] Found renderItemInFirstPerson method");
                return new RenderItemMethodVisitor(api, mv);
            }
            
            return mv;
        }
    }
    
    private static class RenderItemMethodVisitor extends MethodVisitor {
        
        public RenderItemMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Inject call to our animation handler at the start of the method
            // AmethystClient.animationHandler.onRenderItemStart(partialTicks);
            mv.visitFieldInsn(Opcodes.GETSTATIC, 
                "com/amethyst/client/AmethystClient", 
                "animationHandler", 
                "Lcom/amethyst/client/AnimationHandler;");
            
            mv.visitVarInsn(Opcodes.FLOAD, 1); // Load partialTicks parameter
            
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "com/amethyst/client/AnimationHandler", 
                "onRenderItemStart", 
                "(F)V", 
                false);
        }
    }
}
