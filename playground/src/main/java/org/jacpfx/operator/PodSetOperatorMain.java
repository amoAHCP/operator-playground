package org.jacpfx.operator;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import org.jacpfx.operator.controller.PodSetController;
import org.jacpfx.operator.crd.DoneablePodSet;
import org.jacpfx.operator.crd.PodSet;
import org.jacpfx.operator.crd.PodSetList;


import java.util.logging.Level;
import java.util.logging.Logger;
public class PodSetOperatorMain {
    public static Logger logger = Logger.getLogger(PodSetOperatorMain.class.getName());

    public static void main(String args[]) {
        try (KubernetesClient client = new DefaultKubernetesClient()) {
            String namespace = client.getNamespace();
            if (namespace == null) {
                logger.log(Level.INFO, "No namespace found via config, assuming default.");
                namespace = "default";
            }

            logger.log(Level.INFO, "Using namespace : " + namespace);
            CustomResourceDefinition podSetCustomResourceDefinition = new CustomResourceDefinitionBuilder()
                    .withNewMetadata().withName("apps.kop.jacpfx.org").endMetadata()
                    .withNewSpec()
                    .withGroup("kop.jacpfx.org")
                    .withVersion("v1alpha1")
                    .withNewNames().withKind("App").withPlural("apps").endNames()
                    .withScope("Namespaced")
                    .endSpec()
                    .build();
            CustomResourceDefinitionContext podSetCustomResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
                    .withVersion("v1alpha1")
                    .withScope("Namespaced")
                    .withGroup("kop.jacpfx.org")
                    .withPlural("apps")
                    .build();

            SharedInformerFactory informerFactory = client.informers();

            MixedOperation<PodSet, PodSetList, DoneablePodSet, Resource<PodSet, DoneablePodSet>> podSetClient = client.customResources(podSetCustomResourceDefinition, PodSet.class, PodSetList.class, DoneablePodSet.class);
            SharedIndexInformer<Pod> podSharedIndexInformer = informerFactory.sharedIndexInformerFor(Pod.class, PodList.class, 10 * 60 * 1000);
            SharedIndexInformer<PodSet> podSetSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(podSetCustomResourceDefinitionContext, PodSet.class, PodSetList.class, 10 * 60 * 1000);
            PodSetController podSetController = new PodSetController(client, podSetClient, podSharedIndexInformer, podSetSharedIndexInformer, namespace);

            podSetController.create();
            informerFactory.startAllRegisteredInformers();

            podSetController.run();
        }
    }
}
