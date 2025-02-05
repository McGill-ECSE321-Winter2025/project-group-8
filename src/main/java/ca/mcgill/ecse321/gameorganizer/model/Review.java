package ca.mcgill.ecse321.gameorganizer.model;

import java.util.Date;

public class Review
{



    private static int nextId = 1;




    private int rating;
    private String comment;
    private Date dateSubmitted;


    private int id;


    private Game reviewedGame;
    private Account reviewer;



    public Review(int aRating, String aComment, Date aDateSubmitted, Game aReviewedGame, Account aReviewer)
    {
        rating = aRating;
        comment = aComment;
        dateSubmitted = aDateSubmitted;
        id = nextId++;
        if (!setReviewedGame(aReviewedGame))
        {
            throw new RuntimeException("Unable to create Review due to aReviewedGame. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setReviewer(aReviewer))
        {
            throw new RuntimeException("Unable to create Review due to aReviewer. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }



    public boolean setRating(int aRating)
    {
        boolean wasSet = false;
        rating = aRating;
        wasSet = true;
        return wasSet;
    }

    public boolean setComment(String aComment)
    {
        boolean wasSet = false;
        comment = aComment;
        wasSet = true;
        return wasSet;
    }

    public boolean setDateSubmitted(Date aDateSubmitted)
    {
        boolean wasSet = false;
        dateSubmitted = aDateSubmitted;
        wasSet = true;
        return wasSet;
    }

    public int getRating()
    {
        return rating;
    }

    public String getComment()
    {
        return comment;
    }

    public Date getDateSubmitted()
    {
        return dateSubmitted;
    }

    public int getId()
    {
        return id;
    }

    public Game getReviewedGame()
    {
        return reviewedGame;
    }

    public Account getReviewer()
    {
        return reviewer;
    }

    public boolean setReviewedGame(Game aNewReviewedGame)
    {
        boolean wasSet = false;
        if (aNewReviewedGame != null)
        {
            reviewedGame = aNewReviewedGame;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setReviewer(Account aNewReviewer)
    {
        boolean wasSet = false;
        if (aNewReviewer != null)
        {
            reviewer = aNewReviewer;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        reviewedGame = null;
        reviewer = null;
    }





    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "rating" + ":" + getRating()+ "," +
                "comment" + ":" + getComment()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateSubmitted" + "=" + (getDateSubmitted() != null ? !getDateSubmitted().equals(this)  ? getDateSubmitted().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "reviewedGame = "+(getReviewedGame()!=null?Integer.toHexString(System.identityHashCode(getReviewedGame())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "reviewer = "+(getReviewer()!=null?Integer.toHexString(System.identityHashCode(getReviewer())):"null");
    }
}
