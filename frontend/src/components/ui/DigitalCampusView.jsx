import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import Card, { CardHeader, CardBody } from './Card';
import Badge from './Badge';
import { Building, DoorOpen, Users, Clock, CheckCircle2, AlertCircle, Wrench } from 'lucide-react';

export const DigitalCampusView = () => {
  const [resources, setResources] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchResources = async () => {
    try {
      const res = await api.get('/resources/availability');
      setResources(res.data);
      if (res.data.length > 0 && !selectedRoom) {
        setSelectedRoom(res.data[0]);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResources();
    const interval = setInterval(fetchResources, 20000);
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return (
      <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)]">
        Scanning digital campus facilities...
      </div>
    );
  }

  const blocks = ['Block A', 'Block B', 'Block C'];

  return (
    <Card>
      <CardHeader
        title="Digital Campus Resource Map"
        subtitle="Real-time occupancy status across lecture halls, computer labs, and seminar centers"
      />
      <CardBody className="p-5">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Room Blocks Grid */}
          <div className="lg:col-span-2 space-y-4">
            {blocks.map((block) => {
              const blockRooms = resources.filter((r) => r.building === block);
              if (blockRooms.length === 0) return null;

              return (
                <div key={block} className="p-3.5 rounded-xl border border-[var(--color-border)] bg-[var(--color-background)]/50">
                  <div className="flex items-center gap-2 mb-2.5 text-xs font-bold text-[var(--color-foreground)]">
                    <Building className="w-3.5 h-3.5 text-[var(--color-primary)]" />
                    <span>{block} Facilities</span>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
                    {blockRooms.map((room) => {
                      const isOccupied = room.currentStatus === 'OCCUPIED';
                      const isSelected = selectedRoom?.id === room.id;

                      return (
                        <button
                          key={room.id}
                          onClick={() => setSelectedRoom(room)}
                          className={`p-3 rounded-lg border text-left transition-all ${
                            isSelected
                              ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/10 ring-2 ring-[var(--color-primary)]/20'
                              : isOccupied
                              ? 'border-amber-200 dark:border-amber-900/40 bg-amber-500/5 hover:border-amber-400'
                              : 'border-[var(--color-border)] bg-[var(--color-card)] hover:border-[var(--color-primary)]/40'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-mono text-xs font-bold text-[var(--color-foreground)]">
                              {room.roomNumber}
                            </span>
                            <span
                              className={`w-2 h-2 rounded-full ${
                                isOccupied ? 'bg-amber-500' : 'bg-emerald-500'
                              }`}
                            />
                          </div>
                          <p className="text-[10px] text-[var(--color-muted-foreground)] mt-1 truncate">
                            {room.name}
                          </p>
                          <div className="mt-2 flex items-center justify-between text-[10px]">
                            <span className="text-[var(--color-muted-foreground)]">{room.type}</span>
                            <span className={isOccupied ? 'text-amber-600 font-semibold' : 'text-emerald-600 font-semibold'}>
                              {isOccupied ? 'In Session' : 'Available'}
                            </span>
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Selected Room Inspector Panel */}
          <div>
            {selectedRoom ? (
              <div className="p-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] space-y-4">
                <div className="border-b border-[var(--color-border)] pb-3">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-base font-bold text-[var(--color-primary)]">
                      {selectedRoom.roomNumber}
                    </span>
                    <Badge variant={selectedRoom.currentStatus === 'OCCUPIED' ? 'warning' : 'success'} size="sm">
                      {selectedRoom.currentStatus === 'OCCUPIED' ? 'Occupied' : 'Available'}
                    </Badge>
                  </div>
                  <h4 className="text-xs font-bold text-[var(--color-foreground)] mt-1">
                    {selectedRoom.name}
                  </h4>
                  <p className="text-[11px] text-[var(--color-muted-foreground)]">
                    {selectedRoom.building} • {selectedRoom.type}
                  </p>
                </div>

                <div className="space-y-3 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[var(--color-muted-foreground)]">Seating Capacity:</span>
                    <span className="font-semibold text-[var(--color-foreground)]">{selectedRoom.capacity} students</span>
                  </div>

                  <div className="p-3 rounded-lg bg-[var(--color-background)] border border-[var(--color-border)] space-y-1.5">
                    <p className="text-[10px] uppercase font-bold tracking-wider text-[var(--color-muted-foreground)]">
                      Scheduled Lecture
                    </p>
                    <p className="font-semibold text-[var(--color-foreground)]">
                      {selectedRoom.activeClass || 'No classes in session'}
                    </p>
                    <p className="text-[11px] text-[var(--color-muted-foreground)] flex items-center gap-1">
                      <Clock className="w-3 h-3 text-[var(--color-primary)]" />
                      <span>{selectedRoom.scheduledTime}</span>
                    </p>
                    {selectedRoom.faculty && selectedRoom.faculty !== 'N/A' && (
                      <p className="text-[11px] text-[var(--color-foreground)]">
                        Instructor: <strong className="text-[var(--color-primary)]">{selectedRoom.faculty}</strong>
                      </p>
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-8 text-center text-xs text-[var(--color-muted-foreground)]">
                Select a facility on the left to inspect its live status and schedule.
              </div>
            )}
          </div>
        </div>
      </CardBody>
    </Card>
  );
};

export default DigitalCampusView;
