package com.ultikits.plugins.social.entity;

import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BlacklistData Entity Tests")
class BlacklistDataTest {

    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {

        @Test
        @DisplayName("Should build entity with all fields")
        void buildWithAllFields() {
            UUID playerUuid = UUID.randomUUID();
            UUID blockedUuid = UUID.randomUUID();
            long timestamp = System.currentTimeMillis();

            BlacklistData data = BlacklistData.builder()
                    .playerUuid(playerUuid.toString())
                    .blockedUuid(blockedUuid.toString())
                    .blockedName("BlockedPlayer")
                    .createdTime(timestamp)
                    .reason("Harassment")
                    .build();

            assertThat(data.getPlayerUuid()).isEqualTo(playerUuid.toString());
            assertThat(data.getBlockedUuid()).isEqualTo(blockedUuid.toString());
            assertThat(data.getBlockedName()).isEqualTo("BlockedPlayer");
            assertThat(data.getCreatedTime()).isEqualTo(timestamp);
            assertThat(data.getReason()).isEqualTo("Harassment");
        }

        @Test
        @DisplayName("Should build entity with minimal fields")
        void buildWithMinimalFields() {
            BlacklistData data = BlacklistData.builder()
                    .playerUuid("player-uuid")
                    .blockedUuid("blocked-uuid")
                    .blockedName("Blocked")
                    .build();

            assertThat(data.getPlayerUuid()).isEqualTo("player-uuid");
            assertThat(data.getBlockedUuid()).isEqualTo("blocked-uuid");
            assertThat(data.getBlockedName()).isEqualTo("Blocked");
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("Should create blacklist entry without reason")
        void createWithoutReason() {
            UUID playerUuid = UUID.randomUUID();
            UUID blockedUuid = UUID.randomUUID();
            long beforeCreate = System.currentTimeMillis();

            BlacklistData data = BlacklistData.create(playerUuid, blockedUuid, "BlockedPlayer");

            long afterCreate = System.currentTimeMillis();

            assertThat(data.getPlayerUuid()).isEqualTo(playerUuid.toString());
            assertThat(data.getBlockedUuid()).isEqualTo(blockedUuid.toString());
            assertThat(data.getBlockedName()).isEqualTo("BlockedPlayer");
            assertThat(data.getCreatedTime()).isBetween(beforeCreate, afterCreate);
            assertThat(data.getReason()).isNull();
        }

        @Test
        @DisplayName("Should create blacklist entry with reason")
        void createWithReason() {
            UUID playerUuid = UUID.randomUUID();
            UUID blockedUuid = UUID.randomUUID();
            long beforeCreate = System.currentTimeMillis();

            BlacklistData data = BlacklistData.create(
                    playerUuid,
                    blockedUuid,
                    "BlockedPlayer",
                    "Spamming"
            );

            long afterCreate = System.currentTimeMillis();

            assertThat(data.getPlayerUuid()).isEqualTo(playerUuid.toString());
            assertThat(data.getBlockedUuid()).isEqualTo(blockedUuid.toString());
            assertThat(data.getBlockedName()).isEqualTo("BlockedPlayer");
            assertThat(data.getCreatedTime()).isBetween(beforeCreate, afterCreate);
            assertThat(data.getReason()).isEqualTo("Spamming");
        }
    }

    @Nested
    @DisplayName("Setters and Getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Should update player UUID")
        void updatePlayerUuid() {
            BlacklistData data = BlacklistData.builder()
                    .playerUuid("old-uuid")
                    .build();

            data.setPlayerUuid("new-uuid");

            assertThat(data.getPlayerUuid()).isEqualTo("new-uuid");
        }

        @Test
        @DisplayName("Should update blocked UUID")
        void updateBlockedUuid() {
            BlacklistData data = BlacklistData.builder()
                    .blockedUuid("old-uuid")
                    .build();

            data.setBlockedUuid("new-uuid");

            assertThat(data.getBlockedUuid()).isEqualTo("new-uuid");
        }

        @Test
        @DisplayName("Should update blocked name")
        void updateBlockedName() {
            BlacklistData data = BlacklistData.builder()
                    .blockedName("OldName")
                    .build();

            data.setBlockedName("NewName");

            assertThat(data.getBlockedName()).isEqualTo("NewName");
        }

        @Test
        @DisplayName("Should update reason")
        void updateReason() {
            BlacklistData data = BlacklistData.builder().build();

            data.setReason("Updated reason");

            assertThat(data.getReason()).isEqualTo("Updated reason");
        }

        @Test
        @DisplayName("Should update created time")
        void updateCreatedTime() {
            BlacklistData data = BlacklistData.builder()
                    .createdTime(1000L)
                    .build();

            data.setCreatedTime(2000L);

            assertThat(data.getCreatedTime()).isEqualTo(2000L);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("Should be equal with same data")
        void equalWithSameData() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Blocked")
                    .createdTime(1000L)
                    .reason("Test")
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Blocked")
                    .createdTime(1000L)
                    .reason("Test")
                    .build();

            assertThat(data1).isEqualTo(data2);
        }

        @Test
        @DisplayName("Should not be equal with different data")
        void notEqualWithDifferentData() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid3")
                    .build();

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void notEqualToNull() {
            BlacklistData data = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .build();

            assertThat(data).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void notEqualToDifferentType() {
            BlacklistData data = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .build();

            assertThat(data).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal to itself")
        void equalToSelf() {
            BlacklistData data = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .build();

            assertThat(data).isEqualTo(data);
        }

        @Test
        @DisplayName("Should have consistent hashCode for equal objects")
        void consistentHashCode() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Blocked")
                    .createdTime(1000L)
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Blocked")
                    .createdTime(1000L)
                    .build();

            assertThat(data1.hashCode()).isEqualTo(data2.hashCode());
        }

        @Test
        @DisplayName("Should have different hashCode for different objects")
        void differentHashCode() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid3")
                    .build();

            assertThat(data1.hashCode()).isNotEqualTo(data2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToStringTests {

        @Test
        @DisplayName("Should include all fields in toString")
        void toStringIncludesFields() {
            BlacklistData data = BlacklistData.builder()
                    .playerUuid("player-uuid-123")
                    .blockedUuid("blocked-uuid-456")
                    .blockedName("BadPlayer")
                    .createdTime(12345L)
                    .reason("Testing")
                    .build();

            String str = data.toString();

            assertThat(str).contains("player-uuid-123");
            assertThat(str).contains("blocked-uuid-456");
            assertThat(str).contains("BadPlayer");
            assertThat(str).contains("12345");
            assertThat(str).contains("Testing");
        }
    }

    @Nested
    @DisplayName("No-Args Constructor")
    class NoArgsConstructor {

        @Test
        @DisplayName("Should create with no-args constructor")
        void createWithNoArgs() {
            BlacklistData data = new BlacklistData();

            assertThat(data.getPlayerUuid()).isNull();
            assertThat(data.getBlockedUuid()).isNull();
            assertThat(data.getBlockedName()).isNull();
            assertThat(data.getCreatedTime()).isZero();
            assertThat(data.getReason()).isNull();
        }
    }

    @Nested
    @DisplayName("All-Args Constructor")
    class AllArgsConstructor {

        @Test
        @DisplayName("Should create with all-args constructor")
        void createWithAllArgs() {
            BlacklistData data = new BlacklistData(
                    "player-uuid", "blocked-uuid", "BlockedName", 9999L, "SomeReason");

            assertThat(data.getPlayerUuid()).isEqualTo("player-uuid");
            assertThat(data.getBlockedUuid()).isEqualTo("blocked-uuid");
            assertThat(data.getBlockedName()).isEqualTo("BlockedName");
            assertThat(data.getCreatedTime()).isEqualTo(9999L);
            assertThat(data.getReason()).isEqualTo("SomeReason");
        }
    }

    @Nested
    @DisplayName("Builder ToString")
    class BuilderToString {

        @Test
        @DisplayName("Builder should have toString")
        void builderToString() {
            String str = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .toString();

            assertThat(str).isNotNull();
            assertThat(str).contains("BlacklistData");
        }
    }

    @Nested
    @DisplayName("Equality with different fields")
    class EqualityDifferentFields {

        @Test
        @DisplayName("Should not be equal with different playerUuid")
        void notEqualDifferentPlayerUuid() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Same")
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid-different")
                    .blockedUuid("uuid2")
                    .blockedName("Same")
                    .build();

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("Should not be equal with different name")
        void notEqualDifferentName() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Name1")
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .blockedName("Name2")
                    .build();

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("Should not be equal with different reason")
        void notEqualDifferentReason() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .reason("Reason1")
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .reason("Reason2")
                    .build();

            assertThat(data1).isNotEqualTo(data2);
        }

        @Test
        @DisplayName("Should not be equal with different createdTime")
        void notEqualDifferentTime() {
            BlacklistData data1 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .createdTime(1000L)
                    .build();

            BlacklistData data2 = BlacklistData.builder()
                    .playerUuid("uuid1")
                    .blockedUuid("uuid2")
                    .createdTime(2000L)
                    .build();

            assertThat(data1).isNotEqualTo(data2);
        }
    }
}
