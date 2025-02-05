package ca.mcgill.ecse321.gameorganizer.model;

import java.util.Date;

public class BorrowRequest
{



    private static int nextId = 1;



    //BorrowRequest Attributes
    private Date startDate;
    private Date endDate;
    private String status;
    private Date requestDate;


    private int id;

    //BorrowRequest Associations
    private Account requestedBy;
    private GameOwner managedBy;
    private Game requestedGame;



    public BorrowRequest(Date aStartDate, Date aEndDate, String aStatus, Date aRequestDate, Account aRequestedBy, GameOwner aManagedBy, Game aRequestedGame)
    {
        startDate = aStartDate;
        endDate = aEndDate;
        status = aStatus;
        requestDate = aRequestDate;
        id = nextId++;
        if (!setRequestedBy(aRequestedBy))
        {
            throw new RuntimeException("Unable to create BorrowRequest due to aRequestedBy. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setManagedBy(aManagedBy))
        {
            throw new RuntimeException("Unable to create BorrowRequest due to aManagedBy. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setRequestedGame(aRequestedGame))
        {
            throw new RuntimeException("Unable to create BorrowRequest due to aRequestedGame. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }



    public boolean setStartDate(Date aStartDate)
    {
        boolean wasSet = false;
        startDate = aStartDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setEndDate(Date aEndDate)
    {
        boolean wasSet = false;
        endDate = aEndDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setStatus(String aStatus)
    {
        boolean wasSet = false;
        status = aStatus;
        wasSet = true;
        return wasSet;
    }

    public boolean setRequestDate(Date aRequestDate)
    {
        boolean wasSet = false;
        requestDate = aRequestDate;
        wasSet = true;
        return wasSet;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public String getStatus()
    {
        return status;
    }

    public Date getRequestDate()
    {
        return requestDate;
    }

    public int getId()
    {
        return id;
    }
    /* Code from template association_GetOne */
    public Account getRequestedBy()
    {
        return requestedBy;
    }
    /* Code from template association_GetOne */
    public GameOwner getManagedBy()
    {
        return managedBy;
    }
    /* Code from template association_GetOne */
    public Game getRequestedGame()
    {
        return requestedGame;
    }
    /* Code from template association_SetUnidirectionalOne */
    public boolean setRequestedBy(Account aNewRequestedBy)
    {
        boolean wasSet = false;
        if (aNewRequestedBy != null)
        {
            requestedBy = aNewRequestedBy;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setManagedBy(GameOwner aNewManagedBy)
    {
        boolean wasSet = false;
        if (aNewManagedBy != null)
        {
            managedBy = aNewManagedBy;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setRequestedGame(Game aNewRequestedGame)
    {
        boolean wasSet = false;
        if (aNewRequestedGame != null)
        {
            requestedGame = aNewRequestedGame;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        requestedBy = null;
        managedBy = null;
        requestedGame = null;
    }





    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "status" + ":" + getStatus()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "startDate" + "=" + (getStartDate() != null ? !getStartDate().equals(this)  ? getStartDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "endDate" + "=" + (getEndDate() != null ? !getEndDate().equals(this)  ? getEndDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestDate" + "=" + (getRequestDate() != null ? !getRequestDate().equals(this)  ? getRequestDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestedBy = "+(getRequestedBy()!=null?Integer.toHexString(System.identityHashCode(getRequestedBy())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "managedBy = "+(getManagedBy()!=null?Integer.toHexString(System.identityHashCode(getManagedBy())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestedGame = "+(getRequestedGame()!=null?Integer.toHexString(System.identityHashCode(getRequestedGame())):"null");
    }
}