package fr.lapinrose.kstream.kstreamdemo;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;

@EnableBinding(IKStreamProcessor.class)
public class KStreamListener {

    @StreamListener
    @SendTo(IKStreamProcessor.OUTPUT)
    public KStream<String, String> process(@Input(IKStreamProcessor.INPUT) KStream<String, String> message){
        return message
                .mapValues( (k,m) -> m.toUpperCase()); // convert message en x Event
    }
}
