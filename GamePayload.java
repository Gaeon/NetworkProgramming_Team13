import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GamePayload {

	public static class GameType implements Serializable {
		private G_Base base;
		public GameType(G_Base base) {
			this.base = base;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}
	}

	public static class GameStartPayload implements Serializable {
		private G_Base base;

		public GameStartPayload(G_Base base) {
			this.base = base;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}
	}

	public static class GameSettingPayload implements Serializable {
		private G_Base base;
		private String liar;
		private String keyword;
		private boolean status;

		// Host
		public GameSettingPayload(G_Base base, String liar, String keyword) {
			this.base = base;
			this.liar = liar;
			this.keyword = keyword;
		}

		// Client
		public GameSettingPayload(G_Base base, boolean status) {
			this.base = base;
			this.status = status;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}

		public String getLiar() {
			return liar;
		}

		public void setLiar(String liar) {
			this.liar = liar;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}
	}

	public static class FirstOpinionPayload implements Serializable {
		private G_Base base;
		private String message;


		// Host
		public FirstOpinionPayload(G_Base base) {
			this.base = base;
		}

		// Client
		public FirstOpinionPayload(G_Base base, String message) {
			this.base = base;
			this.message = message;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	public static class ChatPayload implements Serializable {
		private G_Base base;
		private String chat;

		// Host
		public ChatPayload(G_Base base) {
			this.base = base;
		}

		// Client
		public ChatPayload(G_Base base, String chat) {
			this.base = base;
			this.chat = chat;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}

		public String getChat() {
			return chat;
		}

		public void setChat(String chat) {
			this.chat = chat;
		}
	}

	public static class VotePayload implements Serializable {
		private G_Base base;
		private List<String> userList;
		private String votee;

		public VotePayload(G_Base base, List<String> userList) {
			this.base = base;
			this.userList = userList;
		}

		public VotePayload(G_Base base, String votee) {
			this.base = base;
			this.votee = votee;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}

		public List<String> getUserList() {
			return userList;
		}

		public void setUserList(List<String> userList) {
			this.userList = userList;
		}
	}

	public static class ResultPayload implements Serializable {
		private G_Base base;
		private String votedLiar;
		private String liar;

		public ResultPayload(G_Base base, String votedLiar, String liar) {
			this.base = base;
			this.votedLiar = votedLiar;
			this.liar = liar;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}

		public String getVotedLiar() {
			return votedLiar;
		}

		public void setVotedLiar(String votedLiar) {
			this.votedLiar = votedLiar;
		}

		public String getLiar() {
			return liar;
		}

		public void setLiar(String liar) {
			this.liar = liar;
		}
	}
}
