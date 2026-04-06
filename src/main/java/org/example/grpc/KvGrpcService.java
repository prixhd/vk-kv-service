package org.example.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.example.grpc.proto.*;
import org.example.repository.TarantoolKvRepository;
import org.example.repository.TarantoolKvRepository.KvResult;

public class KvGrpcService extends KVServiceGrpc.KVServiceImplBase {

    private final TarantoolKvRepository repo;

    public KvGrpcService(TarantoolKvRepository repo) {
        this.repo = repo;
    }

    private BytesValue toProtoBytes(byte[] value) {
        return BytesValue.of(ByteString.copyFrom(value));
    }

    @Override
    public void put(PutRequest req, StreamObserver<PutResponse> resp) {
        try {
            byte[] value;
            if (req.hasValue()) {
                value = req.getValue().getValue().toByteArray();
            } else {
                value = null;
            }

            repo.put(req.getKey(), value);
            resp.onNext(PutResponse.newBuilder().setSuccess(true).build());
            resp.onCompleted();

        } catch (Exception e) {
            resp.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void get(GetRequest req, StreamObserver<GetResponse> resp) {
        try {
            KvResult result = repo.get(req.getKey());

            GetResponse.Builder builder = GetResponse.newBuilder()
                    .setExists(result.isFound());

            if (result.isFound()) {
                builder.setKey(result.getKey());

                if (result.getValue() != null) {
                    builder.setValue(toProtoBytes(result.getValue()));
                }
            }

            resp.onNext(builder.build());
            resp.onCompleted();

        } catch (Exception e) {
            resp.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void delete(DeleteRequest req, StreamObserver<DeleteResponse> resp) {
        try {
            boolean deleted = repo.delete(req.getKey());
            resp.onNext(DeleteResponse.newBuilder().setSuccess(deleted).build());
            resp.onCompleted();

        } catch (Exception e) {
            resp.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void range(RangeRequest req, StreamObserver<KeyValuePair> resp) {
        try {
            repo.range(req.getKeySince(), req.getKeyTo(), result -> {
                KeyValuePair.Builder pair = KeyValuePair.newBuilder()
                        .setKey(result.getKey());

                if (result.getValue() != null) {
                    pair.setValue(toProtoBytes(result.getValue()));
                }

                resp.onNext(pair.build());
            });

            resp.onCompleted();

        } catch (Exception e) {
            resp.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void count(CountRequest req, StreamObserver<CountResponse> resp) {
        try {
            long count = repo.count();
            resp.onNext(CountResponse.newBuilder().setCount(count).build());
            resp.onCompleted();

        } catch (Exception e) {
            resp.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}