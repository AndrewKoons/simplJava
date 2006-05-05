/*
 * Created on Apr 6, 2006
 */
package ecologylab.services.authentication;

import ecologylab.generic.ObjectRegistry;
import ecologylab.services.ServicesClient;
import ecologylab.services.authentication.messages.AuthMessages;
import ecologylab.services.authentication.messages.Login;
import ecologylab.services.messages.ResponseMessage;
import ecologylab.xml.NameSpace;

/**
 * Represents the client side of an authenticating server. Requires that it is
 * connected and authenticated with the server before it can begin attempting to
 * process messages.
 * 
 * @author Zach Toups (toupsz@gmail.com)
 */
public class ServicesClientAuthentication extends ServicesClient implements
        AuthMessages
{
    private AuthenticationListEntry entry = null;

    private boolean loggedIn = false;

    /**
     * @return Returns loggedIn.
     */
    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    /**
     * Creates a new ServicesClientAuthentication object using the given
     * parameters.
     * 
     * @param server
     * @param port
     */
    public ServicesClientAuthentication(String server, int port)
    {
        this(server, port, null);
    }

    /**
     * Creates a new ServicesClientAuthentication object using the given
     * parameters.
     * 
     * @param server
     * @param port
     * @param messageSpace
     * @param objectRegistry
     */
    public ServicesClientAuthentication(String server, int port,
            NameSpace messageSpace, ObjectRegistry objectRegistry)
    {
        this(server, port, messageSpace, objectRegistry, null);
    }

    /**
     * Creates a new ServicesClientAuthentication object using the given
     * parameters.
     * 
     * @param server
     * @param port
     * @param entry
     */
    public ServicesClientAuthentication(String server, int port,
            AuthenticationListEntry entry)
    {
        this(server, port, NameSpace.get("authClient",
                "ecologylab.services.authentication"), new ObjectRegistry(),
                entry);
    }

    /**
     * Main constructor; creates a new ServicesClientAuthentication using the
     * parameters.
     * 
     * @param server
     * @param port
     * @param messageSpace
     * @param objectRegistry
     * @param entry
     */
    public ServicesClientAuthentication(String server, int port,
            NameSpace messageSpace, ObjectRegistry objectRegistry,
            AuthenticationListEntry entry)
    {
        super(server, port, messageSpace, objectRegistry);

        messageSpace.addTranslation("ecologylab.services.authentication.messages",
                "Login");
        messageSpace.addTranslation("ecologylab.services.authentication.messages",
                "Logout");
        messageSpace.addTranslation("ecologylab.services.authentication",
                "AuthenticationListEntry");

        messageSpace.addTranslation("ecologylab.services.messages",
                "OkResponse");
        messageSpace.addTranslation("ecologylab.services.messages",
                "BadSemanticContentResponse");
        messageSpace.addTranslation("ecologylab.services.messages",
                "ErrorResponse");

        this.entry = entry;
    }

    /**
     * @param entry
     *            The entry to set.
     */
    public void setEntry(AuthenticationListEntry entry)
    {
        this.entry = entry;
    }

    /**
     * Attempts to connect to the server using the AuthenticationListEntry that
     * is associated with the client's side of the connection. Returns true if
     * the client is connected and authenticated; false otherwise.
     * 
     * Has the side effect of disconnecting completely if authentication fails.
     */
    public boolean connect()
    {
        ResponseMessage response = null;

        super.connect();

        // if we have an entry (username + password), then we can try to connect
        // to the server.
        if (entry != null)
        {
            response = this.sendMessage(new Login(entry));

            if (response.isOK())
            {
                loggedIn = true;
            } else
            {
                loggedIn = false;
            }

        } else
        {
            loggedIn = false;
        }

        if (!loggedIn)
        {
            super.disconnect();
        }

        return this.connected();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ecologylab.services.ServicesClient#processResponse(ecologylab.services.messages.ResponseMessage)
     */
    protected void processResponse(ResponseMessage responseMessage)
    {
        // TODO Auto-generated method stub
        super.processResponse(responseMessage);
    }

    /**
     * Indicates whether or not the client is connected AND authenticated.
     */
    public boolean connected()
    {
        return (loggedIn && super.connected());
    }
}
