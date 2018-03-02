package com.hazeluff.discord.nhlbot;

class Test {

	int a = 1;
	Test() {

	}

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(() -> {
			System.out.println("start");
			while (true)
				System.out.println("asdf");
		});
		t.start();
		System.out.println(t.isInterrupted());
		t.interrupt();
		System.out.println(t.isInterrupted());
	}
}