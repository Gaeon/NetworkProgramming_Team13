package LiarGame;

public class Data {
    public record C_Base(
            int type,
            String sender,
            String receiver,
            long time,
            String roomId
    ) {}

    public record C_refresh(
            C_Base base
    ) {}

    public record C_gameroommake(
            C_Base base,
            String topic
    ) {}

    public record C_gameroomcancle(
            C_Base base
    ) {}

    public record C_gameroomenter(
            C_Base base
    ) {}

    public record C_gameroomexit(
            C_Base base
    ) {}

    public record C_gameroominfo(
            C_Base base
            //게임방 정보들(client id 등)
    ) {}

    public record C_gameroomstart(
            C_Base base
    ) {}
    public record RoomInfo(

    ){
    }
}
