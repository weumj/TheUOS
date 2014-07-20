package com.uoscs09.theuos.common.impl.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fragment 또는 Activity가 파괴될 때,<br>
 * <b>({@code Fragment.onDetach()}또는 {@code Activity.onDestroy()} 가 호출된 경우)</b><br>
 * 해제될 Field를 나타낸다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseWhenDestroy {
}
