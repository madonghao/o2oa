/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.bbs.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.Integer;
import java.lang.String;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.bbs.entity.BBSVoteOption.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 11:58:33 CST 2018")
public class BBSVoteOption_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<BBSVoteOption,Integer> chooseCount;
    public static volatile SingularAttribute<BBSVoteOption,String> creatorName;
    public static volatile SingularAttribute<BBSVoteOption,String> forumId;
    public static volatile SingularAttribute<BBSVoteOption,String> id;
    public static volatile SingularAttribute<BBSVoteOption,String> mainSectionId;
    public static volatile SingularAttribute<BBSVoteOption,String> optionContentType;
    public static volatile SingularAttribute<BBSVoteOption,String> optionGroupId;
    public static volatile SingularAttribute<BBSVoteOption,String> optionPictureId;
    public static volatile SingularAttribute<BBSVoteOption,String> optionTextContent;
    public static volatile SingularAttribute<BBSVoteOption,Integer> orderNumber;
    public static volatile SingularAttribute<BBSVoteOption,String> sectionId;
    public static volatile SingularAttribute<BBSVoteOption,String> subjectId;
}
