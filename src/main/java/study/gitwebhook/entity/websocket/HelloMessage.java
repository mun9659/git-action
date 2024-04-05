package study.gitwebhook.entity.websocket;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class HelloMessage {

    private String name;

    public HelloMessage(String name) {
        this.name = name;
    }

}
