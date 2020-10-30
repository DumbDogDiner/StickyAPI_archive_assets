/* 
 *  StickyAPI - Utility methods, classes and potentially code-dupe-annihilating code for DDD plugins
 *  Copyright (C) 2020 DumbDogDiner <dumbdogdiner.com>
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dumbdogdiner.stickyapi.common.util;

import java.util.Random;
import java.util.zip.CRC32;

final class Luhn {
    private Luhn() {}

    /**
     * Checks if the card is valid
     * 
     * @param card {@link String} card number
     * @return result {@link boolean} true of false
     */
    public static boolean luhnCheck(String card) {
        if (card == null)
            return false;

        char checkDigit = card.charAt(card.length() - 1);
        String digit = calculateCheckDigit(card.substring(0, card.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    /**
     * Calculates the last digits for the card number received as parameter
     * 
     * @param card {@link String} number
     * @return {@link String} the check digit
     */
    public static String calculateCheckDigit(String card) {
        if (card == null)
            return null;

        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];

        for (int i = 0; i < card.length(); i++)
            digits[i] = Character.getNumericValue(card.charAt(i));

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2) {
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10)
                digits[i] = digits[i] - 9;
        }

        int sum = 0;
        for (int i = 0; i < digits.length; i++)
            sum += digits[i];

        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }
}

/**
 * Class for generating small semi-unique IDs
 */
public class ShortID {

    private String id;

    public ShortID() {
        Random r = new Random();
        this.id = generate(r.nextInt()).toString();
    }

    protected ShortID(String name) {
        this.id = name;
    }

    public String toString() {
        return id;
    }

    /**
     * Create a ShortID from a string
     * @param name 
     * Returns a ShortID object from the string inputed
     * @throws IllegalArgumentException If name does not conform to the string representation as described in toString
     */
    public static ShortID fromString(String name) throws IllegalArgumentException {
        if (!validateID(name))
            throw new IllegalArgumentException("The provided String is not a valid Luhn-parseable ShortID");
        return new ShortID((name));
    }

    /**
     * Generate a short semi-unique ID
     * 
     * @param key An identifier to start the ID with (usually the position in a
     *            SQL table)
     * @return a Luhn-passable identifier
     */
    public static ShortID generate(int key) {
        CRC32 crc = new CRC32();

        crc.update((int) (System.currentTimeMillis() / 1000L));
        // Some kind of semi-unique identifier (like last database row insertion)
        crc.update(key);

        // Get the hash
        String crc32hash = Long.toHexString(crc.getValue());
        // Calculate the Luhn check digit
        crc32hash += Luhn.calculateCheckDigit(crc32hash);
        // return.
        return new ShortID(crc32hash.toUpperCase());
    }

    /**
     * Generate a short semi-unique ID
     * 
     * @return a Luhn-passable identifier
     */
    public static ShortID generate() {
        CRC32 crc = new CRC32();

        crc.update((int) (System.nanoTime()));

        // Get the hash
        String crc32hash = Long.toHexString(crc.getValue());
        // Calculate the Luhn check digit
        crc32hash += Luhn.calculateCheckDigit(crc32hash);
        // return.
        return new ShortID(crc32hash.toUpperCase());
    }

    /**
     * Validate that a string passes the Luhn test
     * 
     * @param id a string to test against the Luhn algorithm
     * @return Whether the string passes the Luhn test
     */
    public static boolean validateID(String id) {
        return Luhn.luhnCheck(id);
    }
}