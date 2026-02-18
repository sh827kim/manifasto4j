package ai.manifesto.sdk;

final class SdkMappings {
    private SdkMappings() {
    }

    static RuntimeKind toSdk(ai.manifesto.runtime.RuntimeKind runtimeKind) {
        if (runtimeKind == null) {
            return RuntimeKind.DOMAIN;
        }
        return RuntimeKind.valueOf(runtimeKind.name());
    }

    static AppStatus toSdk(ai.manifesto.runtime.AppStatus status) {
        if (status == null) {
            return AppStatus.CREATED;
        }
        return AppStatus.valueOf(status.name());
    }

    static ActionPhase toSdk(ai.manifesto.runtime.ActionPhase phase) {
        if (phase == null) {
            return ActionPhase.FAILED;
        }
        return ActionPhase.valueOf(phase.name());
    }

    static ActionUpdate toSdk(ai.manifesto.runtime.ActionUpdate update) {
        return new ActionUpdate(toSdk(update.phase()), update.message(), update.timestampMillis());
    }

    static AppHead toSdk(ai.manifesto.runtime.AppHead head) {
        if (head == null) {
            return null;
        }
        return new AppHead(head.branchName(), head.worldId(), head.createdAt());
    }

    static ActionResult toSdk(ai.manifesto.runtime.ActionResult actionResult) {
        if (actionResult == null) {
            return null;
        }
        if (actionResult instanceof ai.manifesto.runtime.CompletedActionResult completed) {
            return new CompletedActionResult(
                completed.status(),
                completed.worldId(),
                toSdk(completed.runtimeKind())
            );
        }
        if (actionResult instanceof ai.manifesto.runtime.FailedActionResult failed) {
            return new FailedActionResult(
                failed.status(),
                failed.reason(),
                failed.worldId(),
                toSdk(failed.runtimeKind())
            );
        }
        if (actionResult instanceof ai.manifesto.runtime.RejectedActionResult rejected) {
            return new RejectedActionResult(
                rejected.status(),
                rejected.reason(),
                toSdk(rejected.runtimeKind())
            );
        }
        if (actionResult instanceof ai.manifesto.runtime.PreparationFailedActionResult preparationFailed) {
            return new PreparationFailedActionResult(
                preparationFailed.status(),
                preparationFailed.reason(),
                toSdk(preparationFailed.runtimeKind())
            );
        }
        return new FailedActionResult(
            actionResult.status(),
            "unknown_result_type",
            null,
            toSdk(actionResult.runtimeKind())
        );
    }

    static StoredMemoryRecord toSdk(ai.manifesto.runtime.StoredMemoryRecord record) {
        return new StoredMemoryRecord(record.key(), record.value(), record.timestamp());
    }

    static RecallResult toSdk(ai.manifesto.runtime.RecallResult result) {
        return new RecallResult(
            result.records().stream().map(SdkMappings::toSdk).toList(),
            result.contextFrozen(),
            result.contextToken(),
            result.failureMarker()
        );
    }

    static ai.manifesto.runtime.StoredMemoryRecord toRuntime(StoredMemoryRecord record) {
        return new ai.manifesto.runtime.StoredMemoryRecord(record.key(), record.value(), record.timestamp());
    }

    static ai.manifesto.runtime.BackfillConfig toRuntime(BackfillConfig config) {
        if (config == null) {
            return ai.manifesto.runtime.BackfillConfig.defaults();
        }
        return new ai.manifesto.runtime.BackfillConfig(config.overwriteExisting());
    }

    static ai.manifesto.runtime.MemoryMaintenanceOptions toRuntime(MemoryMaintenanceOptions options) {
        if (options == null) {
            return ai.manifesto.runtime.MemoryMaintenanceOptions.defaults();
        }
        return new ai.manifesto.runtime.MemoryMaintenanceOptions(options.maxEntries());
    }

    static ai.manifesto.runtime.RecallRequest toRuntime(RecallRequest request) {
        if (request == null) {
            return new ai.manifesto.runtime.RecallRequest("", 0);
        }
        return new ai.manifesto.runtime.RecallRequest(
            request.keyPrefix(),
            request.limit(),
            request.freezeContext(),
            request.contextToken()
        );
    }
}
