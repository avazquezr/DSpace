/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.ObjectUtils;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.ReloadableEntity;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxyHelper;

/**
 * Database entity representation of the ResourcePolicy table
 *
 * @author kevinvandevelde at atmire.com
 */
@Entity
@Table(name="resourcepolicy")
public class ResourcePolicy implements ReloadableEntity<Integer> {
    public static String TYPE_SUBMISSION = "TYPE_SUBMISSION";
    public static String TYPE_WORKFLOW = "TYPE_WORKFLOW";
    public static String TYPE_CUSTOM= "TYPE_CUSTOM";
    public static String TYPE_INHERITED= "TYPE_INHERITED";

    @Id
    @Column(name="policy_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE ,generator="resourcepolicy_seq")
    @SequenceGenerator(name="resourcepolicy_seq", sequenceName="resourcepolicy_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.PERSIST}, targetEntity = DSpaceObject.class)
    @JoinColumn(name = "dspace_object")
    private AuthorizableEntity dSpaceObject;

    /*
     * {@see org.dspace.core.Constants#Constants Constants}
     */
    @Column(name = "resource_type_id")
    private Integer resourceTypeId;

    @Column(name = "resource_id")
    private Integer resourceId;
    
    /*
     * {@see org.dspace.core.Constants#Constants Constants}
     */
    @Column(name="action_id")
    private Integer actionId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eperson_id")
    private EPerson eperson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="epersongroup_id")
    private Group epersonGroup;

    @Column(name="start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name="end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name="rpname", length = 30)
    private String rpname;


    @Column(name="rptype", length = 30)
    private String rptype;

    @Lob
    @Type(type="org.hibernate.type.MaterializedClobType")
    @Column(name="rpdescription")
    private String rpdescription;

    /**
     * Protected constructor, create object using:
     * {@link org.dspace.authorize.service.ResourcePolicyService#create(Context)}
     */
    protected ResourcePolicy()
    {

    }

    /**
     * Return true if this object equals obj, false otherwise.
     *
     * @param obj
     *     object to compare (eperson, group, start date, end date, ...)
     * @return true if ResourcePolicy objects are equal
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        Class<?> objClass = HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
        if (getClass() != objClass)
        {
            return false;
        }
        final ResourcePolicy other = (ResourcePolicy) obj;
        if (getAction() != other.getAction())
        {
            return false;
        }
        if (!ObjectUtils.equals(getEPerson(), other.getEPerson()))
        {
            return false;
        }
        if (!ObjectUtils.equals(getGroup(), other.getGroup()))
        {
            return false;
        }
        if (!ObjectUtils.equals(getStartDate(), other.getStartDate()))
        {
            return false;
        }
        if (!ObjectUtils.equals(getEndDate(), other.getEndDate()))
        {
            return false;
        }
        return true;
    }

    /**
     * Return a hash code for this object.
     *
     * @return int hash of object
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + this.getAction();
        if (this.getGroup() != null)
        {
            hash = 19 * hash + this.getGroup().hashCode();
        } else {
            hash = 19 * hash + -1;
        }

        if (this.getEPerson() != null)
        {
            hash = 19 * hash + this.getEPerson().hashCode();
        } else {
            hash = 19 * hash + -1;
        }

        hash = 19 * hash + (this.getStartDate() != null ? this.getStartDate().hashCode() : 0);
        hash = 19 * hash + (this.getEndDate() != null ? this.getEndDate().hashCode() : 0);
        return hash;
    }

    /**
     * Get the ResourcePolicy's internal identifier
     *
     * @return the internal identifier
     */
    public Integer getID() {
        return id;
    }

    public AuthorizableEntity getdSpaceObject() {
        return dSpaceObject;
    }

    public void setdSpaceObject(AuthorizableEntity dSpaceObject) {
    	//FIXME BTW maybe is usefull manage a CrisObject to build authorize with CrisAuthorizeManager
    	if(dSpaceObject.getType()>=9) {
    		this.resourceId = dSpaceObject.getLegacyId();
    	}
    	else {
    		this.dSpaceObject = dSpaceObject;
    	}
        this.resourceTypeId = dSpaceObject.getType();
    }

    /**
     * set the action this policy authorizes
     *
     * @param myid  action ID from {@link org.dspace.core.Constants#Constants Constants}
     */
    public void setAction(int myid)
    {
        this.actionId = myid;
    }

    /**
     * @return get the action this policy authorizes
     */
    public int getAction()
    {
        return actionId;
    }

    /**
     * @return eperson, null if EPerson not set
     */
    public EPerson getEPerson()
    {
        return eperson;
    }

    /**
     * assign an EPerson to this policy
     * @param eperson Eperson
     */
    public void setEPerson(EPerson eperson)
    {
        this.eperson = eperson;
    }

    /**
     * gets the Group referred to by this policy
     *
     * @return group, or null if no group set
     */
    public Group getGroup()
    {
        return epersonGroup;
    }

    /**
     * sets the Group referred to by this policy
     * @param epersonGroup Group
     */
    public void setGroup(Group epersonGroup)
    {
        this.epersonGroup = epersonGroup;
    }

    /**
     * Get the start date of the policy
     *
     * @return start date, or null if there is no start date set (probably most
     *         common case)
     */
    public java.util.Date getStartDate()
    {
        return startDate;
    }

    /**
     * Set the start date for the policy
     *
     * @param d
     *            date, or null for no start date
     */
    public void setStartDate(java.util.Date d)
    {
        startDate = d;
    }

    /**
     * Get end date for the policy
     *
     * @return end date or null for no end date
     */
    public java.util.Date getEndDate()
    {
        return endDate;
    }

    /**
     * Set end date for the policy
     *
     * @param d
     *            end date, or null
     */
    public void setEndDate(java.util.Date d)
    {
        this.endDate = d;
    }

    public String getRpName() {
        return rpname;
    }
    public void setRpName(String name) {
        this.rpname = name;
    }

    public String getRpType() {
        return rptype;
    }
    public void setRpType(String type) {
        this.rptype = type;
    }

    public String getRpDescription() {
        return rpdescription;
    }
    public void setRpDescription(String description) {
        this.rpdescription = description;
    }
    
    /**
     * figures out if the date is valid for the policy
     * 
     * @return true if policy has begun and hasn't expired yet (or no dates are
     *         set)
     */
    public boolean isDateValid()
    {
        Date sd = getStartDate();
        Date ed = getEndDate();

        // if no dates set, return true (most common case)
        if ((sd == null) && (ed == null))
        {
            return true;
        }

        // one is set, now need to do some date math
        Date now = new Date();

        // check start date first
        if (sd != null && now.before(sd))
        {
            // start date is set, return false if we're before it
            return false;
        }

        // now expiration date
        if (ed != null && now.after(ed))
        {
            // end date is set, return false if we're after it
            return false;
        }

        // if we made it this far, start < now < end
        return true; // date must be okay
    }

	public int getResourceTypeId() {
		return resourceTypeId;
	}

	public void setResourceTypeId(int resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

}
