import java.io.Serializable;

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

		public GameSettingPayload(G_Base base, String liar, String keyword, boolean status) {
			this.base = base;
			this.liar = liar;
			this.keyword = keyword;
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

	// Payload for G_Explain event
	public static class FirstOpinionPayload implements Serializable {
		private G_Base base;
		private String message;

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

	// Payload for G_Discuss event
	public static class ChatPayload implements Serializable {
		private G_Base base;
		private String chat;

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

	// Payload for G_Vote event
	public static class VotePayload implements Serializable {
		private G_Base base;
		// Add any G_Vote-specific fields here

		public VotePayload(G_Base base) {
			this.base = base;
		}

		public G_Base getBase() {
			return base;
		}

		public void setBase(G_Base base) {
			this.base = base;
		}
	}

	// Payload for G_Result event
	public static class ResultPayload implements Serializable {
		private G_Base base;
		private String votedLiar;
		private String liar;
		private boolean winner;

		public ResultPayload(G_Base base, String votedLiar, String liar, boolean winner) {
			this.base = base;
			this.votedLiar = votedLiar;
			this.liar = liar;
			this.winner = winner;
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

		public boolean isWinner() {
			return winner;
		}

		public void setWinner(boolean winner) {
			this.winner = winner;
		}
	}
}
