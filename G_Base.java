import java.io.Serializable;

public class G_Base implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String type;
	private String sender;
	private String receiver;
	private String timestamp;

	public G_Base(int id, String type, String sender, String receiver, String timestamp) {
		this.id = id;
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.timestamp = timestamp;
	}

	// Getter and Setter methods
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "G_Base{" +
				"id=" + id +
				", type='" + type + '\'' +
				", sender='" + sender + '\'' +
				", receiver='" + receiver + '\'' +
				", timestamp=" + timestamp +
				'}';
	}
}
