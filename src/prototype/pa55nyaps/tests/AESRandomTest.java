/*
PA55 NYAPS Java Reference Implementation

Copyright 2015 Anirban Basu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package prototype.pa55nyaps.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import prototype.pa55nyaps.crypto.AESRandom;

/**
 * This class tests the output of the AES based random number generator with an expected
 * set of outputs, which is consistent across the implementation of the random number generator
 * in Objective C.
 * 
 * @author Anirban Basu
 *
 */
public class AESRandomTest {

	@Test
	public void testIntegers() {
		System.out.println("Executing: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		try {
			byte[] seed = "0123456789abcdef".getBytes("UTF-8");
			int[] expectedInts = {194713050, 189047029, 85839812, 132172331, 28331064, 6743273, 9491039, 5022996, 2239045, 1405757, 618064, 247678, 147778, 111950, 48067, 24523, 13443, 1503, 67, 1206, 605, 279, 43, 34, 17, 2, 5, 3, 0};
			
			AESRandom drbg = new AESRandom(seed);
			for(int i = (1<<30) - 1, j=0; i>2; j++) {
				int next = drbg.nextInt(i);
				System.out.println("\tNext integer: " + next);
				assertEquals(expectedInts[j], next);
				i = ((i+1)>>1) - 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Finished: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	
	@Test
	public void testBytes() {
		System.out.println("Executing: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		try {
			byte[] seed = "0123456789abcdef".getBytes("UTF-8");
			int[] expectedBytes = {11, 27, 21, 26, 11, 4, 0};
			
			AESRandom drbg = new AESRandom(seed);
			
			for(int i = 255, j=0; i>2; j++) {
				int next = drbg.nextByte(i);
				System.out.println("\tNext byte: " + next);
				assertEquals(expectedBytes[j], next);
				i = ((i+1)>>1) - 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Finished: " + Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

}
