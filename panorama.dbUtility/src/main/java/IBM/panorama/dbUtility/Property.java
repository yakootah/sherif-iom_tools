package IBM.panorama.dbUtility;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;
import org.hibernate.annotations.Filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DataTransferObject
@Table(name = "Properties", schema = "Panorama")
@Filter(name = "activeFilter", condition = ":active")
public class Property implements DataObject
{
	@Id
	@GeneratedValue
	private long id;
	@RemoteProperty
	private String name;
	@RemoteProperty
	private String value;
	private String desc;
	@RemoteProperty
	private boolean optional;
	@RemoteProperty
	private boolean active;
}
