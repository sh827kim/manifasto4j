package ai.manifesto.world.persistence;

/**
 * KR: world persistence 계층에서 사용하는 표준 오류 코드입니다.
 * EN: Standard error codes used by world persistence layer.
 */
public enum WorldErrorCode {
    INTERNAL_ERROR,
    INVALID_ARGUMENT,
    WORLD_ALREADY_EXISTS,
    WORLD_NOT_FOUND,
    GENESIS_ALREADY_SET,
    EDGE_ALREADY_EXISTS,
    EDGE_SOURCE_WORLD_NOT_FOUND,
    EDGE_TARGET_WORLD_NOT_FOUND,
    PROPOSAL_ALREADY_EXISTS,
    PROPOSAL_NOT_FOUND,
    DECISION_ALREADY_EXISTS,
    DECISION_ALREADY_EXISTS_FOR_PROPOSAL,
    BINDING_NOT_FOUND
}
