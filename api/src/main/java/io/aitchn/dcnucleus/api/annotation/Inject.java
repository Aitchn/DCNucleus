package io.aitchn.dcnucleus.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 執行時能反射到
@Target(ElementType.FIELD)          // 用在欄位上
public @interface Inject {
}
