package de.teamA.SWT.entities;

/**
 * Four status are available: - Borrowed: The physical is currently borrowed. -
 * Reserved: The physical has been returned and is now available to borrow for
 * the next user on the reservation list - Available: The physical is neither
 * reserved nor borrowed. - Unavailable: The owner of the physical does not
 * allow to borrow the book.
 */
public enum PhysicalStatus {
    BORROWED, RESERVED, AVAILABLE, UNAVAILABLE
}