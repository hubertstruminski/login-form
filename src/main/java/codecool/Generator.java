package codecool;

public class Generator {

    public static String setSessionId(){
        String alphabet = "ABCDEFGHIJKLMNOPRSTUWXYZ";
        String numbers = "1234567890";

        StringBuilder builder = new StringBuilder();

        for(int i=0; i<10; i++){

            int indexOfAlphabet = (int)(Math.random() * alphabet.length());
            char letter = alphabet.charAt(indexOfAlphabet);
            builder.append(letter);

            int indexOfNumbers = (int)(Math.random() * numbers.length());
            char digit = numbers.charAt(indexOfNumbers);
            builder.append(digit);
        }
        return builder.toString();
    }
}
