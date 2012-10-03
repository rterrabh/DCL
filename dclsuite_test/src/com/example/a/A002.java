package com.example.a;

import com.example.b.B002;
import com.example.c.C002;

public class A002 {

	public void f() {
		B002 objB = new B002();
		C002 objC = new C002();
		
		System.out.println(objB.fieldB);
		System.out.println(objC.fieldC);
	}

}
