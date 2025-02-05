package ca.mcgill.ecse321.gameorganizer.model;

import java.util.Date;

public class Event
{


    private static int nextId = 1;



    //Event Attributes
    private String title;
    private Date dateTime;
    private String location;
    private String description;
    private int maxParticipants;


    private int id;

    //Event Associations
    private Account createdBy;
    private Game featuredGame;



    public Event(String aTitle, Date aDateTime, String aLocation, String aDescription, int aMaxParticipants, Account aCreatedBy, Game aFeaturedGame)
    {
        title = aTitle;
        dateTime = aDateTime;
        location = aLocation;
        description = aDescription;
        maxParticipants = aMaxParticipants;
        id = nextId++;
        if (!setCreatedBy(aCreatedBy))
        {
            throw new RuntimeException("Unable to create Event due to aCreatedBy. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setFeaturedGame(aFeaturedGame))
        {
            throw new RuntimeException("Unable to create Event due to aFeaturedGame. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }



    public boolean setTitle(String aTitle)
    {
        boolean wasSet = false;
        title = aTitle;
        wasSet = true;
        return wasSet;
    }

    public boolean setDateTime(Date aDateTime)
    {
        boolean wasSet = false;
        dateTime = aDateTime;
        wasSet = true;
        return wasSet;
    }

    public boolean setLocation(String aLocation)
    {
        boolean wasSet = false;
        location = aLocation;
        wasSet = true;
        return wasSet;
    }

    public boolean setDescription(String aDescription)
    {
        boolean wasSet = false;
        description = aDescription;
        wasSet = true;
        return wasSet;
    }

    public boolean setMaxParticipants(int aMaxParticipants)
    {
        boolean wasSet = false;
        maxParticipants = aMaxParticipants;
        wasSet = true;
        return wasSet;
    }

    public String getTitle()
    {
        return title;
    }

    public Date getDateTime()
    {
        return dateTime;
    }

    public String getLocation()
    {
        return location;
    }

    public String getDescription()
    {
        return description;
    }

    public int getMaxParticipants()
    {
        return maxParticipants;
    }

    public int getId()
    {
        return id;
    }

    public Account getCreatedBy()
    {
        return createdBy;
    }

    public Game getFeaturedGame()
    {
        return featuredGame;
    }

    public boolean setCreatedBy(Account aNewCreatedBy)
    {
        boolean wasSet = false;
        if (aNewCreatedBy != null)
        {
            createdBy = aNewCreatedBy;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setFeaturedGame(Game aNewFeaturedGame)
    {
        boolean wasSet = false;
        if (aNewFeaturedGame != null)
        {
            featuredGame = aNewFeaturedGame;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        createdBy = null;
        featuredGame = null;
    }





    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "title" + ":" + getTitle()+ "," +
                "location" + ":" + getLocation()+ "," +
                "description" + ":" + getDescription()+ "," +
                "maxParticipants" + ":" + getMaxParticipants()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateTime" + "=" + (getDateTime() != null ? !getDateTime().equals(this)  ? getDateTime().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "createdBy = "+(getCreatedBy()!=null?Integer.toHexString(System.identityHashCode(getCreatedBy())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "featuredGame = "+(getFeaturedGame()!=null?Integer.toHexString(System.identityHashCode(getFeaturedGame())):"null");
    }
}
