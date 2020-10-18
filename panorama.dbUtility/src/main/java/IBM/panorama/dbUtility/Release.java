package IBM.panorama.dbUtility;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RELEASES", schema = "Panorama")
public class Release implements DataObject

{
	@Id
	@GeneratedValue
	private long id;
	private String name;
	private String value;
	@Column(name = "release_date")
	private Calendar releaseDate;
}
