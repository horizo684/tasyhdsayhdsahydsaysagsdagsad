# Animations Module - Инструкция по установке

## Текущий статус

✅ **Работает:**
- GUI для настройки анимаций (все слайдеры и переключатели функциональны)
- Сохранение/загрузка настроек
- Пресеты 1.7 и 1.8
- Модуль автоматически включен по умолчанию

⚠️ **Требуется дополнительная интеграция:**
Для полного применения анимаций в игре требуется модификация базовых классов Minecraft.

## Проблема

Minecraft 1.8+ использует `ItemRenderer` для рендеринга предметов в руках игрока. Чтобы изменить анимации, нужно модифицировать методы:
- `ItemRenderer.renderItemInFirstPerson()`
- `ItemRenderer.transformFirstPersonItem()`

## Решения

### Вариант 1: Использование ASM (CoreMod)
Требует создание CoreMod с трансформером классов.

**Необходимые файлы:**
```
src/main/java/com/amethyst/asm/
  ├── AmethystTransformer.java
  ├── ClassTransformer.java
  └── ItemRendererTransformer.java
```

### Вариант 2: Использование Mixin (Рекомендуется)
Более современный и безопасный подход.

**Добавьте в build.gradle:**
```gradle
dependencies {
    implementation 'org.spongepowered:mixin:0.8.5'
}
```

**Создайте файл `mixins.amethyst.json`:**
```json
{
  "required": true,
  "package": "com.amethyst.client.mixins",
  "refmap": "mixins.amethyst.refmap.json",
  "mixins": [
    "MixinItemRenderer"
  ]
}
```

**Создайте класс `MixinItemRenderer.java`:**
```java
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void onRenderItem(float partialTicks, CallbackInfo ci) {
        AnimationHandler handler = getHandler();
        if (handler != null) {
            ItemStack stack = mc.thePlayer.getHeldItem();
            handler.applyItemTransforms(stack, partialTicks);
        }
    }
}
```

### Вариант 3: Forge Events (Текущая реализация)
Частичная поддержка через события Forge.

**Что работает:**
- Old Damage (красная броня при уроне) - через `RenderPlayerEvent`
- Отслеживание параметров анимаций

**Что НЕ работает без ASM/Mixin:**
- Позиционирование предметов (itemPosX, itemPosY, itemPosZ)
- Анимации блокирования, свинга, лука, удочки
- Изменение скорости свинга

## Быстрый старт (с текущей версией)

1. **Скомпилируйте и запустите клиент**
2. **Откройте GUI модулей** (по умолчанию Right Shift)
3. **Найдите модуль Animations** и кликните на него
4. **Настройте параметры:**
   - Переключатели работают (вкл/выкл анимаций)
   - Слайдеры работают (изменение значений)
   - Пресеты 1.7/1.8 работают
   - Все настройки сохраняются

5. **Частичная работа:**
   - Old Damage (красная броня) - работает
   - Остальные эффекты требуют интеграции с ItemRenderer

## Для разработчиков

Класс `AnimationHandler.java` содержит всю логику применения анимаций. Методы готовы к использованию через ASM/Mixin:

- `applyItemTransforms()` - основной метод для трансформации предметов
- `applyPunchingAnimation()` - анимация удара кулаком
- События Forge для других эффектов

## Следующие шаги

Чтобы анимации работали полностью:

1. Выберите один из вариантов интеграции (ASM или Mixin)
2. Создайте соответствующие классы трансформации
3. Вызовите методы из `AnimationHandler` в нужных местах
4. Перекомпилируйте проект

## Поддержка

Если нужна помощь с интеграцией ASM/Mixin, я могу помочь создать необходимые файлы.
