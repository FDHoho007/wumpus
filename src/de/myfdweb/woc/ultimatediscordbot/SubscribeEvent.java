package de.myfdweb.woc.ultimatediscordbot;

import net.dv8tion.jda.api.events.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SubscribeEvent {

    Class<? extends Event>[] value();

}
