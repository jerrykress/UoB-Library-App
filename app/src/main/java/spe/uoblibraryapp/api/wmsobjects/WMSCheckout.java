package spe.uoblibraryapp.api.wmsobjects;

import spe.uoblibraryapp.api.WMSException;
import spe.uoblibraryapp.api.ncip.WMSNCIPStaffService;

public class WMSCheckout {
    private String bookId;
    private WMSUserProfile userProfile;
    private WMSNCIPStaffService staffService;
    private Boolean rejected;
    private Boolean accepted;

    public WMSCheckout(
            String bookId,
            WMSUserProfile userProfile,
            WMSNCIPStaffService staffService
    ){
        this.bookId = bookId;
        this.staffService = staffService;
        this.userProfile = userProfile;


    }

    /**
     * Exists for the UI people to get book info for a checkout
     * @return
     */
    public WMSBook getBook(){
        return new WMSBook(bookId);
    }

    /**
     * Used to accept a checkout after confirmation
     * @return this returns false if there was an error checking out the book.
     */
    public Boolean accept() throws WMSException{
        if (rejected) {
            // Wooooaaaaah! You cant accept a checkout after you've rejected it, make a new one. 🤬
            throw new WMSException();
        }

        this.accepted = true;
        return null;
    }

    /**
     * Used to reject a checkout after confirmation
     * Essentially invalidates this checkout.
     */
    public void reject() throws WMSException{
        if (accepted) {
            // just wait a sec and think about it... Q: can you undo a book checkout?
            // A: no you cant.
            throw new WMSException(); // Ideally this should be throw new YouAreAnIdiotException()
        }
        this.rejected = true;
    }

}
