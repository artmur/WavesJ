package com.wavesplatform.wavesj.json.deser;

import com.wavesplatform.wavesj.*;
import com.wavesplatform.wavesj.matcher.Order;
import com.wavesplatform.wavesj.transactions.AliasTransactionV1;
import com.wavesplatform.wavesj.transactions.AliasTransactionV2;
import com.wavesplatform.wavesj.transactions.ExchangeTransaction;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

public class ExchangeTransactionDeserTest extends TransactionDeserTest {
    Order sell = new Order(Order.Type.SELL, new AssetPair(Asset.WAVES, "9ZDWzK53XT5bixkmMwTJi2YzgxCqn5dUajXFcT2HcFDy"), 3, 5000000000L, 1526992336241L, 1529584336241L, 2, new PublicKeyAccount("7E9Za8v8aT6EyU1sX91CVK7tWUeAetnNYDxzKZsyjyKV", (byte)'T'), new PublicKeyAccount("Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP", (byte)'T'), new ByteString("2R6JfmNjEnbXAA6nt8YuCzSf1effDS4Wkz8owpCD9BdCNn864SnambTuwgLRYzzeP5CAsKHEviYKAJ2157vdr5Zq"));
    Order buy = new Order(Order.Type.BUY, new AssetPair(Asset.WAVES, "9ZDWzK53XT5bixkmMwTJi2YzgxCqn5dUajXFcT2HcFDy"), 2, 6000000000L, 1526992336241L, 1529584336241L, 1, new PublicKeyAccount("BqeJY8CP3PeUDaByz57iRekVUGtLxoow4XxPvXfHynaZ", (byte)'T'), new PublicKeyAccount("Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP", (byte)'T'), new ByteString("2bkuGwECMFGyFqgoHV4q7GRRWBqYmBFWpYRkzgYANR4nN2twgrNaouRiZBqiK2RJzuo9NooB9iRiuZ4hypBbUQs"));

    ExchangeTransaction tx = new ExchangeTransaction(
            2, 5000000000L, buy, sell, 1, 1, 1, 1526992336241L,
            new ByteString("5NxNhjMrrH5EWjSFnVnPbanpThic6fnNL48APVAkwq19y2FpQp4tNSqoAZgboC2ykUfqQs9suwBQj6wERmsWWNqa")
    );

    @Test
    public void V1DeserializeTest() throws IOException {
        deserializationTest("{\"type\":7,\"id\":\"FaDrdKax2KBZY6Mh7K3tWmanEdzZx6MhYUmpjV3LBJRp\",\"sender\":\"3N22UCTvst8N1i1XDvGHzyqdgmZgwDKbp44\",\"senderPublicKey\":\"Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP\",\"fee\":1,\"timestamp\":1526992336241,\"signature\":\"5NxNhjMrrH5EWjSFnVnPbanpThic6fnNL48APVAkwq19y2FpQp4tNSqoAZgboC2ykUfqQs9suwBQj6wERmsWWNqa\",\"order1\":{\"id\":\"EdUTcUZNK3NYKuPrsPCkZGzVUwpjx6qVjd4TgBwna7po\",\"sender\":\"3MthkhReCHXeaPZcWXcT3fa6ey1XWptLtwj\",\"senderPublicKey\":\"BqeJY8CP3PeUDaByz57iRekVUGtLxoow4XxPvXfHynaZ\",\"matcherPublicKey\":\"Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP\",\"assetPair\":{\"amountAsset\":null,\"priceAsset\":\"9ZDWzK53XT5bixkmMwTJi2YzgxCqn5dUajXFcT2HcFDy\"},\"orderType\":\"buy\",\"price\":6000000000,\"amount\":2,\"timestamp\":1526992336241,\"expiration\":1529584336241,\"matcherFee\":1,\"signature\":\"2bkuGwECMFGyFqgoHV4q7GRRWBqYmBFWpYRkzgYANR4nN2twgrNaouRiZBqiK2RJzuo9NooB9iRiuZ4hypBbUQs\"},\"order2\":{\"id\":\"DS9HPBGRMJcquTb3sAGAJzi73jjMnFFSWWHfzzKK32Q7\",\"sender\":\"3MswjKzUBKCD6i1w4vCosQSbC8XzzdBx1mG\",\"senderPublicKey\":\"7E9Za8v8aT6EyU1sX91CVK7tWUeAetnNYDxzKZsyjyKV\",\"matcherPublicKey\":\"Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP\",\"assetPair\":{\"amountAsset\":null,\"priceAsset\":\"9ZDWzK53XT5bixkmMwTJi2YzgxCqn5dUajXFcT2HcFDy\"},\"orderType\":\"sell\",\"price\":5000000000,\"amount\":3,\"timestamp\":1526992336241,\"expiration\":1529584336241,\"matcherFee\":2,\"signature\":\"2R6JfmNjEnbXAA6nt8YuCzSf1effDS4Wkz8owpCD9BdCNn864SnambTuwgLRYzzeP5CAsKHEviYKAJ2157vdr5Zq\"},\"price\":5000000000,\"amount\":2,\"buyMatcherFee\":1,\"sellMatcherFee\":1}", tx, ExchangeTransaction.class);
    }

    @Test
    public void V2DeserializeTest() throws IOException {
        deserializationTest("{\"type\":7,\"id\":\"FaDrdKax2KBZY6Mh7K3tWmanEdzZx6MhYUmpjV3LBJRp\",\"sender\":\"3N22UCTvst8N1i1XDvGHzyqdgmZgwDKbp44\",\"senderPublicKey\":\"Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP\",\"fee\":1,\"timestamp\":1526992336241,\"signature\":\"5NxNhjMrrH5EWjSFnVnPbanpThic6fnNL48APVAkwq19y2FpQp4tNSqoAZgboC2ykUfqQs9suwBQj6wERmsWWNqa\",\"order1\":{\"id\":\"EdUTcUZNK3NYKuPrsPCkZGzVUwpjx6qVjd4TgBwna7po\",\"sender\":\"3MthkhReCHXeaPZcWXcT3fa6ey1XWptLtwj\",\"senderPublicKey\":\"BqeJY8CP3PeUDaByz57iRekVUGtLxoow4XxPvXfHynaZ\",\"matcherPublicKey\":\"Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP\",\"assetPair\":{\"amountAsset\":null,\"priceAsset\":\"9ZDWzK53XT5bixkmMwTJi2YzgxCqn5dUajXFcT2HcFDy\"},\"orderType\":\"buy\",\"price\":6000000000,\"amount\":2,\"timestamp\":1526992336241,\"expiration\":1529584336241,\"matcherFee\":1,\"signature\":\"2bkuGwECMFGyFqgoHV4q7GRRWBqYmBFWpYRkzgYANR4nN2twgrNaouRiZBqiK2RJzuo9NooB9iRiuZ4hypBbUQs\"},\"order2\":{\"id\":\"DS9HPBGRMJcquTb3sAGAJzi73jjMnFFSWWHfzzKK32Q7\",\"sender\":\"3MswjKzUBKCD6i1w4vCosQSbC8XzzdBx1mG\",\"senderPublicKey\":\"7E9Za8v8aT6EyU1sX91CVK7tWUeAetnNYDxzKZsyjyKV\",\"matcherPublicKey\":\"Fvk5DXmfyWVZqQVBowUBMwYtRAHDtdyZNNeRrwSjt6KP\",\"assetPair\":{\"amountAsset\":null,\"priceAsset\":\"9ZDWzK53XT5bixkmMwTJi2YzgxCqn5dUajXFcT2HcFDy\"},\"orderType\":\"sell\",\"price\":5000000000,\"amount\":3,\"timestamp\":1526992336241,\"expiration\":1529584336241,\"matcherFee\":2,\"signature\":\"2R6JfmNjEnbXAA6nt8YuCzSf1effDS4Wkz8owpCD9BdCNn864SnambTuwgLRYzzeP5CAsKHEviYKAJ2157vdr5Zq\"},\"price\":5000000000,\"amount\":2,\"buyMatcherFee\":1,\"sellMatcherFee\":1}", tx, ExchangeTransaction.class);
    }
}
