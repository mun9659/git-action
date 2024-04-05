package study.gitwebhook.entity.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Greeting {

    private String content;

    public Greeting(String content) {
        this.content = content;
    }

}
