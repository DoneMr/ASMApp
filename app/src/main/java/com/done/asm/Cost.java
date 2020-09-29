package com.done.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * File:com.done.asm.Cost
 * Description:增加耗时计算的注解
 *
 * @author maruilong
 * @date 2020/9/24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public  @interface Cost {
}
