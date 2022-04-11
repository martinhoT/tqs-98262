package euromillions;

import java.util.Arrays;
import java.util.Objects;

import sets.SetOfNaturals;

import java.util.Random;

/**
 * A set of 5 numbers and 2 stars according to the Euromillions ranges.
 *
 * @author ico0
 */
public class Dip {

    public static final int NUMBER_RANGE_MIN = 1;
    public static final int NUMBER_RANGE_MAX = 50;
    public static final int NUMBER_QUANTITY = 5;
    public static final int STAR_RANGE_MIN = 1;
    public static final int STAR_RANGE_MAX = 12;
    public static final int STAR_QUANTITY = 2;

    private SetOfNaturals numbers;
    private SetOfNaturals starts;

    public Dip() {
        numbers = new SetOfNaturals();
        starts = new SetOfNaturals();
    }

    public Dip(int[] arrayOfNumbers, int[] arrayOfStars) {
        this();

        if (NUMBER_QUANTITY != arrayOfNumbers.length || STAR_QUANTITY != arrayOfStars.length)
            throw new IllegalArgumentException("wrong number of elements in numbers/stars");

        if (!Arrays.stream(arrayOfNumbers).allMatch(this::validNumber) || !Arrays.stream(arrayOfStars).allMatch(this::validStar))
            throw new IllegalArgumentException("numbers/stars are out of range");

        numbers.add(arrayOfNumbers);
        starts.add(arrayOfStars);

    }

    private boolean validNumber(int number) {
        return number >= NUMBER_RANGE_MIN && number <= NUMBER_RANGE_MAX;
    }

    private boolean validStar(int star) {
        return star >= STAR_RANGE_MIN && star <= STAR_RANGE_MAX;
    }

    public SetOfNaturals getNumbersColl() {
        return numbers;
    }

    public SetOfNaturals getStarsColl() {
        return starts;
    }

    public static Dip generateRandomDip() {
        Random generator = new Random();

        Dip randomDip = new Dip();
        for (int i = 0; i < NUMBER_QUANTITY; ) {
            int candidate = generator.nextInt(NUMBER_RANGE_MAX) + NUMBER_RANGE_MIN;
            if (!randomDip.getNumbersColl().contains(candidate)) {
                randomDip.getNumbersColl().add(candidate);
                i++;
            }
        }
        for (int i = 0; i < STAR_QUANTITY; ) {
            int candidate = generator.nextInt(STAR_RANGE_MAX) + STAR_RANGE_MIN;
            if (!randomDip.getStarsColl().contains(candidate)) {
                randomDip.getStarsColl().add(candidate);
                i++;
            }
        }
        return randomDip;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.numbers);
        hash = 29 * hash + Objects.hashCode(this.starts);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Dip other = (Dip) obj;
        if (!Objects.equals(this.numbers, other.numbers)) {
            return false;
        }
        return Objects.equals(this.starts, other.starts);
    }


    /**
     * prepares a string representation of the data structure, formated for
     * printing
     *
     * @return formatted string with data
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("N[");
        for (int number : getNumbersColl()) {
            sb.append(String.format("%3d", number));
        }
        sb.append("] S[");
        for (int star : getStarsColl()) {
            sb.append(String.format("  %d", star));
        }
        sb.append("]");
        return sb.toString();
    }
}
