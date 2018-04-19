package com.wysong.model;

import com.wysong.id.Base62IdGenerator;
import org.springframework.util.ObjectUtils;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.io.Serializable;


/**
 * Root parent class for all persistent object entities.
 * <p/>
 * <b>NOTE:</b>  Subclasses should <b>always</b> provide a unique {@link #onEquals onEquals} and
 * {@link #hashCode hasCode} implementation and these should <em>not</em> use
 * the {@link #getId id} property during their execution.  Always keep in mind the entity's 'business keys' aka
 * 'natural keys' when implementing these two methods to prevent duplicate data in the system.
 *
 */
@MappedSuperclass
public abstract class BasicEntity implements Serializable, Cloneable {

    public static final long serialVersionUID = 54035349408L; //"wysong" converted from base62 to base10

    /**
     * Base62 encoded primary key.  See the {@link #getId()} JavaDoc for more info.
     */
    @Id
    private String id;
    /**
     * Used for optimistic locking strategies.  See the {@link #getEntityVersion() entityVersion} JavaDoc for more info.
     */
    private long entityVersion;

    /**
     * Creates the unique id for entities before they are initially persisted to a persistent store.
     */
    @PrePersist
    private void ensureId() {
        if (id == null) {
            this.setId(Base62IdGenerator.getId());
        }
    }

    /**
     * Returns the Base62 encoded primary key.  A null return value means the object has not yet been persisted to the
     * persistent store.
     *
     * @return the base62 encoded unique id or null if the object has not yet been persisted.
     */
    public String getId() {
        return id;
    }

    /**
     * Should <em>never</em> be called directly, but only via JPA or Hibernate or other EIS framework.
     * <p/>
     * This method can be removed entirely if the EIS framework supports setting the ID property
     * directly (e.g. through reflection).  Hibernate does support this, it is called 'property access'.
     *
     * @param id the id to set
     * @return this object for chaining purposes.
     */
    public BasicEntity setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the entity's persistent version number which is used for optimistic locking strategies to ensure
     * two threads (even across different machines) don't simultaneously overwrite entity state.  Its value is for
     * framework support and should rarely, if ever, be referenced by the application.
     * <p>
     * This property is not necessarily used by all subclasses, but it is pretty much required if in a
     * high-concurrency environment and/or if using distributed caching in a cluster.   It (and its corresponding
     * mutator methods) is not called 'version' to prevent eliminating that name from subclasses should the
     * business domain require a property of that name.  Also <em>entityVersion</em> is self-documenting and leaves
     * little room for incorrect interpretation.
     *
     * @return the entity's persistent version number used in optimistic locking strategies - rarely if ever referenced
     *         by the application.
     */
    public long getEntityVersion() {
        return entityVersion;
    }

    /**
     * Should <em>never</em> be called directly.  Only via JPA or Hibernate or other EIS framework
     * <p>
     * This method can be removed entirely if the EIS framework supports setting the property
     * directly (e.g. through reflection).  Hibernate does support this, it is called 'property access'.
     *
     * @param entityVersion the entity version to set for optimistic locking strategies
     */
    public void setEntityVersion(long entityVersion) {
        this.entityVersion = entityVersion;
    }

    /**
     * This method is declared final and does a lot of performance optimization:
     * <p>
     * It delegates the actual "equals" check to subclasses via the onEquals method, but
     * it will only do so if the object for equality comparison is
     * <ol>
     * <li>not the same memory location as the current object (fast sanity check)</li>
     * <li>is <code>instanceof</code> Entity</li>
     * <li>Does not have the same id() property</li>
     * </ol>
     * #3 is important:  this is because if two different entities have the ID property
     * already populated, then they have already been inserted in the database, and
     * because of unique constraints on the database (i.e. your 'business key'), you
     * can <em>guarantee</em> that the objects are not the same and there is no need
     * to incur attribute-based comparisons for equals() checks.
     * <p>
     * This little technique is a massive performance improvement given the number of times
     * equals checks happen in most applications.
     * <p>
     * <b>IMPLEMENTATION NOTE:</b>.  When writing your {@code onEquals} implementation, <em>never</em>
     * perform equals comparisons of class attributes by referencing them directly, i.e.:
     * <pre>       *bad code*
     * this.someAttribute != null ? this.someAttribute.equals( other.someAttribute ) : other.someAttribute == null;</pre>
     * <p>
     * This is because direct property access will bypass any Hibernate proxy entirely.  Instead, the accessor methods
     * must be used to allow the proxy to initialize itself if necessary to load the data being checked for equality:
     * <pre>       *good code*
     * getSomeAttribute() != null ? getSomeAttribute().equals(other.getSomeAttribute()) : other.getSomeAttribute() == null;</pre>
     */
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof BasicEntity) {
            BasicEntity e = (BasicEntity) o;
            String thisId = getId();
            String otherId = e.getId();
            if (thisId != null && otherId != null) {
                return thisId.equals(otherId) &&
                        //the 'isAssignableFrom' checks exist due to proxy objects.  The classes of proxies won't
                        //equal the non-proxied class, but they will be assignable:
                        (getClass().isAssignableFrom(e.getClass()) || e.getClass().isAssignableFrom(getClass()));
            } else {
                return onEquals(e);
            }
        }
        return false;
    }

    /**
     * Subclasses must do an equals comparison based on business keys, aka 'natural keys' here.  Do <em>NOT</em> use
     * the {@link #getId() id} property in these checks.
     *
     * @param e the entity with which to perform attribute-based equality.
     * @return true if the current object is semantically equal to the specified object
     */
    protected abstract boolean onEquals(BasicEntity e);

    protected static boolean nullSafeEquals(Object o1, Object o2) {
        return ObjectUtils.nullSafeEquals(o1, o2);
    }

    protected static int nullSafeHashCode(Object o) {
        return ObjectUtils.nullSafeHashCode(o);
    }

    /**
     * Standard hashCode implementation, but because of the requirements of {@link #onEquals(BasicEntity) onEquals} this
     * method must be semantically correct with <code>onEquals</code> - its calculation should be based on the same
     * <em>business key</em> that is used to calculate <code>onEquals</code>.
     *
     * @return the object's hash code, based on the <code>onEquals</code> business key fields.
     */
    public abstract int hashCode();

    /**
     * If children classes override this method they must always call super.clone() to get the object
     * with which they manipulate further to clone remaining attributes.  Never acquire
     * the cloned object directly via 'new' operator (this is true in Java for any class - it is not special to
     * this Entity class).
     */
    protected Object clone() throws CloneNotSupportedException {
        try {
            BasicEntity cloneEntity = (BasicEntity) super.clone();
            cloneEntity.setId(null);
            cloneEntity.setEntityVersion(-1);
            return cloneEntity;
        } catch (CloneNotSupportedException e) {
            // shouldn't ever happen since this class is Cloneable and
            // a direct subclass of object.
            throw new InternalError("Unable to clone object of type {" + getClass().getName() + "}");
        }
    }

    /**
     * Returns a StringBuilder representing the toString function of the class implementation. This will
     * append the class name and the id before calling the {@link #onStringBuilder(StringBuilder)}.
     *
     * @return a <tt>StringBuilder</tt> representing the <tt>toString</tt> value of this object.
     */
    public StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append("class name = ").append(getClass().getName()).append(", id = ").append(id);
        return onStringBuilder(sb);
    }

    /**
     * Returns a StringBuilder representing the toString function of the class implementation. This
     * should be overridden by all children classes to represent the object in a meaningful String format.
     *
     * @return a <tt>StringBuilder</tt> representing the <tt>toString</tt> value of this object.
     */
    public abstract StringBuilder onStringBuilder(StringBuilder sb);

    /**
     * Returns <code>toStringBuilder().toString()</code>.  Declared as 'final' to require subclasses to override
     * the {@link #toStringBuilder()} method, a cleaner and better performing mechanism for toString();
     *
     * @return toStringBuilder().toString()
     */
    public final String toString() {
        return toStringBuilder().insert(0, '[').append(']').toString();
    }

}
