package LiarGame;

import java.util.List;

public class GData {
	public record G_Base(
			String id,			// 게임방 아이디
			int type,		// 제어 타입
			String sender,
			String receiver,
			long time
	) {}

	public record G_GameSetting(
			G_Base base,
			String liar,
			String keyword,
			boolean status
	) {}

	public record G_GameStart(
			G_Base base
	){}

	public record G_FirstOpinion (
			G_Base base,
			String message
	) {}

	public record G_Chat (
			G_Base base,
			String chat
	) {}

	public record G_Vote(
			G_Base base,
			List<String> participants_name,
			String votedLiar
	) {}

	public record G_Result(
			G_Base base,
			String votedLiar,
			String liar,
			boolean winner
	) {}
}
