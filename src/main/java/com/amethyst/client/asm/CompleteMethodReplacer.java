package com.amethyst.client.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

/**
 * РАДИКАЛЬНЫЙ подход - ПОЛНОСТЬЮ заменяем метод transformFirstPersonItem
 * на свою версию с 1.7 blockhit
 */
public class CompleteMethodReplacer implements IClassTransformer {
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.renderer.ItemRenderer")) {
            System.out.println("[CompleteReplacer] Completely replacing transformFirstPersonItem method");
            return transformItemRenderer(basicClass);
        }
        return basicClass;
    }
    
    private byte[] transformItemRenderer(byte[] classBytes) {
        ClassReader classReader = new ClassReader(classBytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = new MethodReplacerVisitor(Opcodes.ASM5, classWriter);
        
        classReader.accept(classVisitor, 0);
        return classWriter.toByteArray();
    }
    
    private static class MethodReplacerVisitor extends ClassVisitor {
        
        public MethodReplacerVisitor(int api, ClassVisitor cv) {
            super(api, cv);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            // Если это transformFirstPersonItem - заменяем ПОЛНОСТЬЮ
            if ((name.equals("transformFirstPersonItem") || name.equals("func_178096_b")) 
                && desc.equals("(FF)V")) {
                System.out.println("[CompleteReplacer] Found transformFirstPersonItem - REPLACING COMPLETELY");
                
                // Создаем новый метод с нашим кодом
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                
                // Полностью перезаписываем метод
                mv.visitCode();
                
                // Генерируем байткод для нашего метода
                generateCustomTransformMethod(mv);
                
                mv.visitMaxs(0, 0); // COMPUTE_MAXS сделает это за нас
                mv.visitEnd();
                
                return null; // Не даем ASM читать оригинальный код
            }
            
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        
        /**
         * Генерирует байткод для кастомного transformFirstPersonItem
         */
        private void generateCustomTransformMethod(MethodVisitor mv) {
            // Код на Java который мы хотим:
            /*
            private void transformFirstPersonItem(float equipProgress, float swingProgress) {
                // Базовые трансформации
                GlStateManager.translate(0.56F, -0.52F, -0.72F);
                GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                
                float swing = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
                float swingSqrt = MathHelper.sin(Math.sqrt(swingProgress) * 3.1415927F);
                
                // Проверяем блокирование
                boolean isBlocking = this.mc.thePlayer != null && 
                                     this.mc.thePlayer.getHeldItem() != null && 
                                     this.mc.thePlayer.isBlocking();
                
                if (isBlocking) {
                    // 1.7 BLOCKHIT
                    GlStateManager.translate(-0.5F, 0.2F, 0.0F);
                    GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
                } else {
                    // Vanilla swing
                    GlStateManager.rotate(-swingSqrt * 40.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(-swing * 20.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(-swing * 80.0F, 1.0F, 0.0F, 0.0F);
                }
            }
            */
            
            // === БАЗОВЫЕ ТРАНСФОРМАЦИИ ===
            
            // GlStateManager.translate(0.56F, -0.52F, -0.72F);
            mv.visitLdcInsn(0.56F);
            mv.visitLdcInsn(-0.52F);
            mv.visitLdcInsn(-0.72F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "translate", 
                "(FFF)V", 
                false);
            
            // GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitVarInsn(Opcodes.FLOAD, 1); // equipProgress
            mv.visitLdcInsn(-0.6F);
            mv.visitInsn(Opcodes.FMUL);
            mv.visitLdcInsn(0.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "translate", 
                "(FFF)V", 
                false);
            
            // GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            mv.visitLdcInsn(45.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitLdcInsn(1.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "rotate", 
                "(FFFF)V", 
                false);
            
            // === ПРОВЕРКА БЛОКИРОВАНИЯ ===
            
            // this.mc.thePlayer
            mv.visitVarInsn(Opcodes.ALOAD, 0); // this
            mv.visitFieldInsn(Opcodes.GETFIELD, 
                "net/minecraft/client/renderer/ItemRenderer", 
                "mc", // или field_78455_a для obfuscated
                "Lnet/minecraft/client/Minecraft;");
            mv.visitFieldInsn(Opcodes.GETFIELD, 
                "net/minecraft/client/Minecraft", 
                "thePlayer", // или field_71439_g
                "Lnet/minecraft/client/entity/EntityPlayerSP;");
            
            // != null
            Label notNull = new Label();
            mv.visitJumpInsn(Opcodes.IFNULL, notNull);
            
            // isBlocking()
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, 
                "net/minecraft/client/renderer/ItemRenderer", 
                "mc",
                "Lnet/minecraft/client/Minecraft;");
            mv.visitFieldInsn(Opcodes.GETFIELD, 
                "net/minecraft/client/Minecraft", 
                "thePlayer",
                "Lnet/minecraft/client/entity/EntityPlayerSP;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                "net/minecraft/client/entity/EntityPlayerSP", 
                "isBlocking", // или func_70632_aY
                "()Z", 
                false);
            
            Label notBlocking = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, notBlocking);
            
            // === 1.7 BLOCKHIT ===
            System.out.println("[CompleteReplacer] Injecting 1.7 blockhit transforms");
            
            // GlStateManager.translate(-0.5F, 0.2F, 0.0F);
            mv.visitLdcInsn(-0.5F);
            mv.visitLdcInsn(0.2F);
            mv.visitLdcInsn(0.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "translate", 
                "(FFF)V", 
                false);
            
            // GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
            mv.visitLdcInsn(30.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitLdcInsn(1.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "rotate", 
                "(FFFF)V", 
                false);
            
            // GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
            mv.visitLdcInsn(-80.0F);
            mv.visitLdcInsn(1.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "rotate", 
                "(FFFF)V", 
                false);
            
            // GlStateManager.rotate(60.0F, 0.0F, 0.0F, 1.0F);
            mv.visitLdcInsn(60.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitLdcInsn(1.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "net/minecraft/client/renderer/GlStateManager", 
                "rotate", 
                "(FFFF)V", 
                false);
            
            // return (выходим после blockhit)
            mv.visitInsn(Opcodes.RETURN);
            
            // === VANILLA SWING (если не блокируется) ===
            mv.visitLabel(notBlocking);
            mv.visitLabel(notNull);
            
            // Вычисляем swing и swingSqrt
            // float swing = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * PI);
            mv.visitVarInsn(Opcodes.FLOAD, 2); // swingProgress
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "net/minecraft/util/MathHelper",
                "sqrt_float", // или func_76129_c
                "(F)F",
                false);
            mv.visitLdcInsn(3.1415927F);
            mv.visitInsn(Opcodes.FMUL);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "net/minecraft/util/MathHelper",
                "sin", // или func_76126_a
                "(F)F",
                false);
            mv.visitVarInsn(Opcodes.FSTORE, 3); // swingSqrt
            
            // GlStateManager.rotate(-swingSqrt * 40.0F, 1.0F, 0.0F, 0.0F);
            mv.visitVarInsn(Opcodes.FLOAD, 3);
            mv.visitLdcInsn(-40.0F);
            mv.visitInsn(Opcodes.FMUL);
            mv.visitLdcInsn(1.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitLdcInsn(0.0F);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "net/minecraft/client/renderer/GlStateManager",
                "rotate",
                "(FFFF)V",
                false);
            
            // return
            mv.visitInsn(Opcodes.RETURN);
        }
    }
}