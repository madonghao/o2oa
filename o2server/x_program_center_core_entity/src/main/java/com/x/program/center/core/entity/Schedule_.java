/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.program.center.core.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.Long;
import java.lang.String;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.program.center.core.entity.Schedule.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 12:00:28 CST 2018")
public class Schedule_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<Schedule,String> application;
    public static volatile SingularAttribute<Schedule,String> className;
    public static volatile SingularAttribute<Schedule,String> cron;
    public static volatile SingularAttribute<Schedule,Long> elapsed;
    public static volatile SingularAttribute<Schedule,Date> fireTime;
    public static volatile SingularAttribute<Schedule,String> id;
    public static volatile SingularAttribute<Schedule,String> node;
    public static volatile SingularAttribute<Schedule,Date> reportTime;
}
