/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.forge.kubernetes;

import io.fabric8.kubernetes.api.Kubernetes;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.fabric8.kubernetes.api.KubernetesHelper.getId;

/**
 * Deletes a replication controller from kubernetes
 */
public class ReplicationControllerDelete extends AbstractKubernetesCommand {
    @Inject
    @WithAttributes(label = "Replication Controller ID", description = "The ID of the replication controller to delete.", required = true)
    UIInput<String> replicationControllerId;


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), getClass())
                .category(Categories.create(CATEGORY))
                .name(CATEGORY + ": Replication Controller Delete")
                .description("Deletes the given replication controller from the kubernetes cloud");
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        super.initializeUI(builder);

        // populate autocompletion options
        replicationControllerId.setCompleter(new UICompleter<String>() {
            @Override
            public Iterable<String> getCompletionProposals(UIContext context, InputComponent<?, String> input, String value) {
                List<String> list = new ArrayList<String>();
                ReplicationControllerList replicationControllers = getKubernetes().getReplicationControllers();
                if (replicationControllers != null) {
                    List<ReplicationController> items = replicationControllers.getItems();
                    if (items != null) {
                        for (ReplicationController item : items) {
                            String id = getId(item);
                            list.add(id);
                        }
                    }
                }
                Collections.sort(list);
                return list;
            }
        });

        builder.add(replicationControllerId);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Kubernetes kubernetes = getKubernetes();

        String idText = replicationControllerId.getValue();
        ReplicationController replicationController = kubernetes.getReplicationController(idText);
        if (replicationController == null) {
            System.out.println("No replicationController for id: " + idText);
        } else {
            executeReplicationController(replicationController);
        }
        return null;
    }

    protected void executeReplicationController(ReplicationController replicationController) throws Exception {
        getKubernetes().deleteReplicationController(getId(replicationController));
    }
}

