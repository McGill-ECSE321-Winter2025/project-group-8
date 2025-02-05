package ca.mcgill.ecse321.gameorganizer.models;

import java.util.Date;

public class LendingRecord
{



    private static int nextId = 1;


    private Date startDate;
    private Date endDate;
    private String status;


    private int id;

    //LendingRecord Associations
    private BorrowRequest request;
    private GameOwner owner;



    public LendingRecord(Date aStartDate, Date aEndDate, String aStatus, BorrowRequest aRequest, GameOwner aOwner)
    {
        startDate = aStartDate;
        endDate = aEndDate;
        status = aStatus;
        id = nextId++;
        if (!setRequest(aRequest))
        {
            throw new RuntimeException("Unable to create LendingRecord due to aRequest. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setOwner(aOwner))
        {
            throw new RuntimeException("Unable to create LendingRecord due to aOwner. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
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

    public int getId()
    {
        return id;
    }

    public BorrowRequest getRequest()
    {
        return request;
    }

    public GameOwner getOwner()
    {
        return owner;
    }

    public boolean setRequest(BorrowRequest aNewRequest)
    {
        boolean wasSet = false;
        if (aNewRequest != null)
        {
            request = aNewRequest;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setOwner(GameOwner aNewOwner)
    {
        boolean wasSet = false;
        if (aNewOwner != null)
        {
            owner = aNewOwner;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        request = null;
        owner = null;
    }


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "status" + ":" + getStatus()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "startDate" + "=" + (getStartDate() != null ? !getStartDate().equals(this)  ? getStartDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "endDate" + "=" + (getEndDate() != null ? !getEndDate().equals(this)  ? getEndDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "request = "+(getRequest()!=null?Integer.toHexString(System.identityHashCode(getRequest())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "owner = "+(getOwner()!=null?Integer.toHexString(System.identityHashCode(getOwner())):"null");
    }
}

