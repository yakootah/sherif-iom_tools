package IBM.panorama.dbUtility;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CATEGORIES", schema = "Panorama")
@DataTransferObject
public class Category implements DataObject

{
	@Id
	@GeneratedValue
	private long id;
	@RemoteProperty
	private String name;
	@RemoteProperty
	private int size;
	@RemoteProperty
	private int chunk;
	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id")
	@RemoteProperty
	private List<Property> properties = new ArrayList<Property>();
	@Transient
	@RemoteProperty
	private boolean selected;
}
