// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.google.common.collect.ImmutableList;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AddressSelectorTest {

    @Test(groups = "unit", expectedExceptions = GoneException.class)
    public void getPrimaryUri_NoAddress() throws Exception {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();
        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        AddressSelector.getPrimaryUri(request, replicaAddresses);
    }

    @Test(groups = "unit", expectedExceptions = GoneException.class, expectedExceptionsMessageRegExp =
        "The requested resource is no longer available at the server. Returned addresses are \\{https://cosmos1/,https://cosmos2/\\}")
    public void getPrimaryUri_NoPrimaryAddress() throws Exception {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos2", Protocol.HTTPS));

        AddressSelector.getPrimaryUri(request, replicaAddresses);
    }

    @Test(groups = "unit")
    public void getPrimaryUri() throws Exception {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Uri res = AddressSelector.getPrimaryUri(request, replicaAddresses);

        assertThat(res).isEqualTo(Uri.create("https://cosmos2/"));
    }

    @Test(groups = "unit")
    public void getPrimaryUri_WithRequestReplicaIndex() throws Exception {
        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(1).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Uri res = AddressSelector.getPrimaryUri(request, replicaAddresses);

        assertThat(res).isEqualTo(Uri.create("https://cosmos2/"));
    }

    @Test(groups = "unit")
    public void resolvePrimaryUriAsync() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.HTTPS);

        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos4", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos5", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(replicaAddresses.toArray(new AddressInformation[0]))).when(addressResolver).resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Matchers.eq(false));

        Uri res = selector.resolvePrimaryUriAsync(request, false).block();

        assertThat(res).isEqualTo(Uri.create("https://cosmos2/"));
    }

    @Test(groups = "unit")
    public void resolveAllUriAsync() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.HTTPS);

        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos4", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos5", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(replicaAddresses.toArray(new AddressInformation[0]))).when(addressResolver).resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Matchers.eq(false));

        List<Uri> res = selector.resolveAllUriAsync(request, true, false).block();

        assertThat(res).isEqualTo(ImmutableList.of(Uri.create("https://cosmos1/"), Uri.create("https://cosmos2/"), Uri.create("https://cosmos3/")));
    }

    @Test(groups = "unit")
    public void resolveAddressesAsync() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.HTTPS);

        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos4", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos5", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(replicaAddresses.toArray(new AddressInformation[0]))).when(addressResolver).resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Matchers.eq(false));

        List<AddressInformation> res = selector.resolveAddressesAsync(request, false).block();

        assertThat(res).isEqualTo(replicaAddresses.stream().filter(a -> a.getProtocolName().equalsIgnoreCase(Protocol.HTTPS.toString())).collect(Collectors.toList()));
    }

    @Test(groups = "unit")
    public void resolveAllUriAsync_RNTBD() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.TCP);

        RxDocumentServiceRequest request = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "rntbd://cosmos1", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "rntbd://cosmos2", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(replicaAddresses.toArray(new AddressInformation[0]))).when(addressResolver).resolveAsync(Mockito.any(RxDocumentServiceRequest.class), Matchers.eq(false));

        List<Uri> res = selector.resolveAllUriAsync(request, true, false).block();

        assertThat(res).isEqualTo(ImmutableList.of(Uri.create("rntbd://cosmos1/"), Uri.create("rntbd://cosmos2/")));
    }

}
