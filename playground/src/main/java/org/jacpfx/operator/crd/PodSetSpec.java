package org.jacpfx.operator.crd;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;

/*
 */
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class PodSetSpec implements KubernetesResource {
    public int getReplicas() {
        return replicas;
    }

    public List<Container> containers;

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    @Override
    public String toString() {
        return "PodSetSpec{" +
                "containers=" + containers +
                ", replicas=" + replicas +
                '}';
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    private int replicas;
}