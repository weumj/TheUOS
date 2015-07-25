package com.uoscs09.theuos2.common;

/**
 * source from JavaCan : 
 * {@link 'http://javacan.tistory.com/entry/close-androidapp-by-successive-back-press'}<br>
 * 뒤로가기 버튼을 두번 눌러 앱을 종료시키는 일을 처리하는 클래스
 */
public class BackPressCloseHandler {
	private long backKeyPressedTime = 0;

	/** @return 뒤로 버튼이 처음 눌린 이후 2초 내에 다시 눌리면 true, 2초가 지난 뒤 눌리면 false */
	public boolean onBackPressed() {
		long current = System.currentTimeMillis();
		if (current <= backKeyPressedTime + 2000) {
			return true;
		} else {
			backKeyPressedTime = current;
			return false;
		}
	}
}
