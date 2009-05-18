package org.opensprout.webtest.configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value={java.lang.annotation.ElementType.METHOD,java.lang.annotation.ElementType.TYPE})
public @interface WebTest {

}
