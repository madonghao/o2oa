/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.processplatform.core.entity.content;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.Boolean;
import java.lang.String;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.processplatform.core.entity.content.Review.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 12:00:00 CST 2018")
public class Review_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<Review,String> application;
    public static volatile SingularAttribute<Review,String> applicationAlias;
    public static volatile SingularAttribute<Review,String> applicationName;
    public static volatile SingularAttribute<Review,Boolean> completed;
    public static volatile SingularAttribute<Review,Date> completedTime;
    public static volatile SingularAttribute<Review,String> completedTimeMonth;
    public static volatile SingularAttribute<Review,String> creatorIdentity;
    public static volatile SingularAttribute<Review,String> creatorPerson;
    public static volatile SingularAttribute<Review,String> creatorUnit;
    public static volatile SingularAttribute<Review,String> id;
    public static volatile SingularAttribute<Review,String> job;
    public static volatile SingularAttribute<Review,String> person;
    public static volatile SingularAttribute<Review,String> process;
    public static volatile SingularAttribute<Review,String> processAlias;
    public static volatile SingularAttribute<Review,String> processName;
    public static volatile SingularAttribute<Review,String> serial;
    public static volatile SingularAttribute<Review,Date> startTime;
    public static volatile SingularAttribute<Review,String> startTimeMonth;
    public static volatile SingularAttribute<Review,String> title;
    public static volatile SingularAttribute<Review,String> work;
    public static volatile SingularAttribute<Review,String> workCompleted;
}
