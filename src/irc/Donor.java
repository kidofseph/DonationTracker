package irc;



/**
 * Created by Nick on 1/26/14.
 * Created to handle the amounts a person donates.
 * This class was a better solution over PircBot Users.
 */
public class Donor {

    private double donated;
    private String name;

    public Donor(String nick, double amt) {
        name = nick;
        donated = amt;
    }

    /**
     * Gets the amount this person has donated.
     *
     * @return The double amount the person has donated.
     */
    public double getDonated() {
        return donated;
    }

    /**
     * Gets the name of the donator.
     *
     * @return The name of the donator.
     */
    public String getName() {
        return name;
    }

    

    /**
     * Adds the specified amount to the donator's donated amount.
     *
     * @param toAdd The amount to add.
     */
    public void addDonated(double toAdd) {
        donated += toAdd;
    }
}