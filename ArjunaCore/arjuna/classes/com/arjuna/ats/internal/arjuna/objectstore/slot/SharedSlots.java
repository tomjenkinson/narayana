/*
 * Copyright The Narayana Authors
 *
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package com.arjuna.ats.internal.arjuna.objectstore.slot;

public interface SharedSlots {
    String getNodeId(); // the nodeIds that this RecoveryManager will manage
    boolean migrate(CloudId from, CloudId to); // move logs between two nodes (assumes that a RecoveryManager is or will be managing toNodeId)
    boolean migrate(CloudId to); // move logs from this node to another node
}