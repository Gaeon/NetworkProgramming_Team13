package LiarGame;

import java.util.List;

public class Data {
    public record C_Base(
            int type,
            String sender,
            String receiver,
            long time,
            String roomId
    ) {}

    public record C_refresh(
            C_Base base,
            Integer participants_num
    ) {}


    public record C_gameroommake(
            C_Base base,
            String GameTopic
    ) {}

    public record C_gameroomcancel(
            C_Base base
    ) {}

    public record C_gameroomenter(
            C_Base base
    ) {}

    public record C_gameroomexit(
            C_Base base
    ) {}

    public record C_gameroomstart(
            C_Base base,
            List<String> participants_name,
            String GameTopic
    ) {}

    public record C_gameroomInfo(
            C_Base base,
            int infotype,//나갔는지 들어왔는지 어떤 타입인지 확인 1이면 들어옴 2면 나감
            String participant,
            Integer participants_num
    ) {}

    public record C_gameroomenter_confirm(
            C_Base base,
            List<String> participants_name,
            String GameTopic
    ) {}
}
