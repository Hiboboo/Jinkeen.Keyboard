### 安全键盘

- 全字符键盘，含26个英文字母（大小写切换），0-9数字以及小数点
- 纯数字键盘，只有0-9纯数字以及小数点（可控制是否允许输入）
- 身份证键盘，含0-9纯数字以及一个英文大写字母`X`

### 使用方法

1、在需要引入的`module`下的`build.gradle`中加入引用（必须）：

```groovy
implementation 'com.jinkeen.base:keyboard:1.0.1'
```

2、在`Application`的`onCreate()`方法内，配置键盘的全局基础样式（可选）：

```kotlin
/** 为键盘设置统一的简单样式 */
object KeyboardStyle {

    /** 键盘宽度（默认横向铺满） */
    var KEYBOARD_WIDTH = WindowManager.LayoutParams.MATCH_PARENT

    /** 按键的文字大小（默认15） */
    var KEY_TEXT_SIZE = 15.0f

    /** 按键的文字颜色（默认黑色） */
    var KEY_TEXT_COLOR = Color.BLACK

    /** 删除键图标（默认红色删除图标） */
    var KEY_DEL_ICON = R.drawable.ic_abs_keyboard_delete
}
```

3、在具体的布局文件中引入安全键盘输入法（必须）：

```xml
<com.jinkeen.keyboard.SafetyInputEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:minHeight="90dp"
    app:si_keyboard_type="NUMBER"
    app:si_decimal_point="false"
    android:hint="数字键盘"
    android:textColor="@color/black"/>
```

其中两个自定义属性：

`si_keyboard_type` 标识当前输入法支持的输入类型，取三个值：

```xml
<!-- 全键盘 -->
<enum name="QWERTY" />
<!-- 数字键盘 -->
<enum name="NUMBER" />
<!-- 身份证键盘 -->
<enum name="IDCARD" />
```

`si_decimal_point` 标识当前的输入是否允许小数点，`true`表示允许，`false`不允许。默认`true`