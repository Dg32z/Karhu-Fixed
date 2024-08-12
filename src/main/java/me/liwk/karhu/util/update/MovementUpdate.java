package me.liwk.karhu.util.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.liwk.karhu.util.location.CustomLocation;

@AllArgsConstructor
@Getter
public final class MovementUpdate {
    public final CustomLocation fromFrom;
    public final CustomLocation from;
    public final CustomLocation to;
    private final boolean ground;
}
